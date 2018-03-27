/*
 * Copyright 2017 trivago N.V.
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

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
import com.trivago.rta.exceptions.filesystem.FileCreationException;
import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FileIO;
import com.trivago.rta.gherkin.GherkinDocumentParser;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import com.trivago.rta.runners.RunnerFileContentRenderer;
import com.trivago.rta.vo.SingleScenario;
import com.trivago.rta.vo.SingleScenarioRunner;

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
public class FeatureFileConverter {

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
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    public void convertToSingleScenariosAndRunners(
            final List<Path> featureFilePaths) throws CucablePluginException {
        logger.log("");
        for (Path featureFilePath : featureFilePaths) {
            convertToSingleScenariosAndRunners(featureFilePath);
        }
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
            throws CucablePluginException {

        String featureFilePathString = featureFilePath.toString();

        if (featureFilePathString == null || featureFilePathString.equals("")) {
            throw new MissingFileException(featureFilePathString);
        }

        List<Integer> lineNumbers = propertyManager.getScenarioLineNumbers();
        List<String> includeScenarioTags = propertyManager.getIncludeScenarioTags();
        List<String> excludeScenarioTags = propertyManager.getExcludeScenarioTags();
        String featureFileContent = fileIO.readContentFromFile(featureFilePathString);

        List<SingleScenario> singleScenarios;
        try {
            singleScenarios =
                    gherkinDocumentParser.getSingleScenariosFromFeature(
                            featureFileContent, lineNumbers, includeScenarioTags, excludeScenarioTags
                    );
        } catch (CucablePluginException e) {
            throw new FeatureFileParseException(featureFilePathString);
        }

        // In case of a provided line number: if there are no scenarios created
        // that means that the provided line number is wrong.
        if (propertyManager.hasValidScenarioLineNumbers() && singleScenarios.size() == 0) {
            throw new CucablePluginException("There is no parseable scenario or scenario outline at line " + lineNumbers);
        }

        for (SingleScenario singleScenario : singleScenarios) {
            String renderedFeatureFileContent = featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario);

            String featureFileName = getFeatureFileNameFromPath(featureFilePath);
            Integer featureCounter = singleFeatureCounters.getOrDefault(featureFileName, 0);
            featureCounter++;
            String scenarioCounterFilenamePart = String.format(SCENARIO_COUNTER_FORMAT, featureCounter);

            for (int testRuns = 1; testRuns <= propertyManager.getNumberOfTestRuns(); testRuns++) {
                String testRunsCounterFilenamePart = String.format(TEST_RUNS_COUNTER_FORMAT, testRuns);

                // Append the scenario and test run counters to the filename.
                // Also add the "_IT" postfix so Maven Failsafe considers it an integration test automatically.
                String generatedFileName =
                        featureFileName
                                .concat(scenarioCounterFilenamePart)
                                .concat(testRunsCounterFilenamePart)
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

                String renderedRunnerFileContent =
                        runnerFileContentRenderer.getRenderedRunnerFileContent(singleScenarioRunner);

                String generatedRunnerFilePath =
                        propertyManager.getGeneratedRunnerDirectory()
                                .concat(PATH_SEPARATOR)
                                .concat(generatedFileName)
                                .concat(RUNNER_FILE_EXTENSION);

                fileIO.writeContentToFile(renderedRunnerFileContent, generatedRunnerFilePath);
            }
        }
        logCompleteMessage(featureFilePathString);
    }

    /**
     * Log the completion message for a feature file.
     *
     * @param featureFileName the name of the processed feature file.
     */
    private void logCompleteMessage(String featureFileName) {
        String logPostfix = ".";
        if (propertyManager.hasValidScenarioLineNumbers()) {
            logPostfix = String.format(" with line number(s) %s.", propertyManager.getScenarioLineNumbers());
        }
        logger.log(String.format("- Processed '%s'%s", featureFileName, logPostfix));
    }

    /**
     * Get the feature file name without the extension and with replaced special chars from the full feature file path.
     *
     * @param featureFilePath The path to the feature file.
     * @return The cleaned up feature file name.
     */
    private String getFeatureFileNameFromPath(final Path featureFilePath) {
        String fullFeatureFileName = featureFilePath.getFileName().toString();
        String featureFileName = fullFeatureFileName.substring(0, fullFeatureFileName.lastIndexOf("."));
        return featureFileName.replaceAll("\\W", "_");
    }
}

