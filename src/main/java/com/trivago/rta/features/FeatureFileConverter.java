/*
 * Copyright 2017 trivago GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
import com.trivago.rta.exceptions.filesystem.FileCreationException;
import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FeatureFileContentRenderer;
import com.trivago.rta.files.FileIO;
import com.trivago.rta.files.RunnerFileContentRenderer;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import com.trivago.rta.vo.ScenarioKey;
import com.trivago.rta.vo.SingleScenarioFeature;
import com.trivago.rta.vo.SingleScenarioRunner;
import gherkin.ast.GherkinDocument;
import gherkin.pickles.Pickle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for converting feature files
 * into single scenario feature files and runners.
 */
@Singleton
public final class FeatureFileConverter {

    private static final String FEATURE_FILE_EXTENSION = ".feature";
    private static final String RUNNER_FILE_EXTENSION = ".java";
    private static final String INTEGRATION_TEST_POSTFIX = "_IT";
    private static final String PATH_SEPARATOR = "/";
    private static final String TEST_RUNS_COUNTER_FORMAT = "_run%03d";
    private static final String SCENARIO_COUNTER_FORMAT = "_scenario%03d";

    private final PropertyManager propertyManager;
    private final GherkinDocumentParser gherkinDocumentParser;
    private final FeatureFileContentRenderer featureFileContentRenderer;
    private final RunnerFileContentRenderer runnerFileContentRenderer;
    private final FileIO fileIO;
    private final CucableLogger logger;

    // Holds the current number of single features per feature key
    // (in a scenario outline, each example yields a single feature with the same key).
    private Map<String, Integer> singleFeatureCounters = new HashMap<>();

    @Inject
    public FeatureFileConverter(
            PropertyManager propertyManager,
            GherkinDocumentParser gherkinDocumentParser,
            FeatureFileContentRenderer featureFileContentRenderer,
            RunnerFileContentRenderer runnerFileContentRenderer,
            FileIO fileIO,
            CucableLogger logger
    ) {
        this.propertyManager = propertyManager;
        this.gherkinDocumentParser = gherkinDocumentParser;
        this.featureFileContentRenderer = featureFileContentRenderer;
        this.runnerFileContentRenderer = runnerFileContentRenderer;
        this.fileIO = fileIO;
        this.logger = logger;
    }

    /**
     * Converts a list of feature files
     *
     * @param featureFilePaths feature files to process
     * @throws FeatureFileParseException see {@link FeatureFileParseException}
     * @throws MissingFileException      see {@link MissingFileException}
     * @throws FileCreationException     see {@link FileCreationException}
     */
    public void convertToSingleScenariosAndRunners(
            final List<Path> featureFilePaths) throws MissingFileException, FeatureFileParseException, FileCreationException {

        logger.info("Cucable - starting conversion...");

        for (Path featureFilePath : featureFilePaths) {
            convertToSingleScenariosAndRunners(featureFilePath);
        }

        logger.info("Cucable - finished conversion.");
    }

    /**
     * Converts all scenarios in the given feature file to single
     * scenario feature files and their respective runners.
     *
     * @param featureFilePath feature file to process.
     * @throws FeatureFileParseException see {@link FeatureFileParseException}
     * @throws MissingFileException      see {@link MissingFileException}
     * @throws FileCreationException     see {@link FileCreationException}
     */
    private void convertToSingleScenariosAndRunners(final Path featureFilePath)
            throws FeatureFileParseException, MissingFileException, FileCreationException {

        if (featureFilePath.toString() == null || featureFilePath.toString().equals("")) {
            throw new MissingFileException(featureFilePath.toString());
        }

        logger.info("Converting " + featureFilePath + " ...");

        GherkinDocument gherkinDocument = gherkinDocumentParser.getGherkinDocumentFromFeatureFile(featureFilePath);

        List<List<String>> scenarioKeywords = gherkinDocumentParser.getKeywordsFromGherkinDocument(gherkinDocument);

        // Break feature file into scenarios
        List<Pickle> scenarios = gherkinDocumentParser.getPicklesFromGherkinDocument(gherkinDocument);

        // remove extension from feature file
        String fullFeatureFileName = featureFilePath.getFileName().toString();
        String featureFileName = fullFeatureFileName.substring(0, fullFeatureFileName.lastIndexOf("."));

        int scenarioKeywordIndex = 0;
        ScenarioKey previousScenarioKey = null;
        for (Pickle scenario : scenarios) {

            logger.info("PickleScenario: " + scenario.getName());

            ScenarioKey currentScenarioKey = new ScenarioKey(scenario);

            // Set the old scenario key to the current one (only done for the very first scenario)
            if (previousScenarioKey == null) {
                previousScenarioKey = currentScenarioKey;
                scenarioKeywordIndex = 0;
            }

            // Determine if the scenario changed from the one before by comparing their keys (does not happen with scenario outlines)
            if (!currentScenarioKey.equals(previousScenarioKey)) {
                previousScenarioKey = currentScenarioKey;
                scenarioKeywordIndex++;
            }

            // Determine new feature file name from the current scenario key's counter
            Integer featureCounter = singleFeatureCounters.getOrDefault(featureFileName, 0);
            featureCounter++;

            // Save scenario information
            SingleScenarioFeature singleFeature =
                    new SingleScenarioFeature(
                            scenario.getTags(),
                            gherkinDocument.getFeature().getName(),
                            scenario.getName(),
                            scenario.getSteps(),
                            scenarioKeywords.get(scenarioKeywordIndex)
                    );
            String renderedFeatureFileContent = featureFileContentRenderer.getRenderedFeatureFileContent(singleFeature);

            // Add counter to filename
            String featureFileCounterPostfix = String.format(SCENARIO_COUNTER_FORMAT, featureCounter);

            for (int testRuns = 1; testRuns <= propertyManager.getNumberOfTestRuns(); testRuns++) {
                String testRunsPostfix = String.format(TEST_RUNS_COUNTER_FORMAT, testRuns);

                // Append "_IT" to the filename so Failsafe considers it an integration test automatically.
                String generatedFileName =
                        featureFileName
                                .concat(featureFileCounterPostfix)
                                .concat(testRunsPostfix)
                                .concat(INTEGRATION_TEST_POSTFIX);

                String generatedFeatureFilePath =
                        propertyManager.getGeneratedFeatureDirectory()
                                .concat(PATH_SEPARATOR)
                                .concat(generatedFileName)
                                .concat(FEATURE_FILE_EXTENSION);
                singleFeatureCounters.put(featureFileName, featureCounter);

                // Save scenario information to new feature file
                fileIO.writeContentToFile(renderedFeatureFileContent, generatedFeatureFilePath);

                // Generate runner for the newly generated single scenario feature file
                SingleScenarioRunner singleScenarioRunner =
                        new SingleScenarioRunner(
                                propertyManager.getSourceRunnerTemplateFile(), generatedFileName);
                String renderedRunnerFileContent = runnerFileContentRenderer.getRenderedRunnerFileContent(singleScenarioRunner);
                String generatedRunnerFilePath =
                        propertyManager.getGeneratedRunnerDirectory()
                                .concat(PATH_SEPARATOR)
                                .concat(generatedFileName)
                                .concat(RUNNER_FILE_EXTENSION);
                fileIO.writeContentToFile(renderedRunnerFileContent, generatedRunnerFilePath);
            }
        }
    }
}
