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

package com.trivago.features;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.FileCreationException;
import com.trivago.exceptions.filesystem.MissingFileException;
import com.trivago.files.FileIO;
import com.trivago.files.FileSystemManager;
import com.trivago.gherkin.GherkinDocumentParser;
import com.trivago.logging.CucableLogger;
import com.trivago.logging.Language;
import com.trivago.properties.PropertyManager;
import com.trivago.runners.RunnerFileContentRenderer;
import com.trivago.vo.CucableFeature;
import com.trivago.vo.FeatureRunner;
import com.trivago.vo.SingleScenario;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.trivago.logging.CucableLogger.CucableLogLevel.COMPACT;
import static com.trivago.logging.CucableLogger.CucableLogLevel.DEFAULT;
import static com.trivago.logging.CucableLogger.CucableLogLevel.MINIMAL;

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
    private static final String FEATURE_COUNTER_FORMAT = "_feature%03d";
    private static final String SCENARIO_COUNTER_FORMAT = "_scenario%03d";

    private final PropertyManager propertyManager;
    private final GherkinDocumentParser gherkinDocumentParser;
    private final FeatureFileContentRenderer featureFileContentRenderer;
    private final RunnerFileContentRenderer runnerFileContentRenderer;
    private final FileIO fileIO;
    private final FileSystemManager fileSystemManager;
    private final CucableLogger logger;

    // Holds the current number of single features per feature key
    // (in a scenario outline, each example yields a single feature with the same key).
    private final Map<String, Integer> singleFeatureCounters = new HashMap<>();

    @Inject
    public FeatureFileConverter(
            PropertyManager propertyManager,
            GherkinDocumentParser gherkinDocumentParser,
            FeatureFileContentRenderer featureFileContentRenderer,
            RunnerFileContentRenderer runnerFileContentRenderer,
            FileIO fileIO,
            FileSystemManager fileSystemManager,
            CucableLogger logger
    ) {
        this.propertyManager = propertyManager;
        this.gherkinDocumentParser = gherkinDocumentParser;
        this.featureFileContentRenderer = featureFileContentRenderer;
        this.runnerFileContentRenderer = runnerFileContentRenderer;
        this.fileIO = fileIO;
        this.fileSystemManager = fileSystemManager;
        this.logger = logger;
    }

    /**
     * Converts a list of Cucable features
     *
     * @param cucableFeatures feature files to process
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    public void generateParallelizableFeatures(
            final List<CucableFeature> cucableFeatures) throws CucablePluginException {

        int featureFileCounter = 0;
        List<String> allGeneratedFeaturePaths = new ArrayList<>();

        for (CucableFeature cucableFeature : cucableFeatures) {
            List<Path> paths = fileSystemManager.getPathsFromCucableFeature(cucableFeature);
            if (paths.size() == 0) {
                logger.warn("No features and runners could be created. Please check your properties!");
            }
            for (Path path : paths) {
                List<String> generatedFeatureFilePaths = generateParallelizableFeatures(path, cucableFeature.getLineNumbers());
                allGeneratedFeaturePaths.addAll(generatedFeatureFilePaths);
                featureFileCounter += generatedFeatureFilePaths.size();
            }
        }

        int runnerFileCounter;
        if (propertyManager.getDesiredNumberOfFeaturesPerRunner() > 0) {
            runnerFileCounter = generateRunnerClassesWithDesiredNumberOfFeatures(
                    allGeneratedFeaturePaths, propertyManager.getDesiredNumberOfFeaturesPerRunner()
            );
        } else {
            runnerFileCounter = generateRunnerClassesWithDesiredNumberOfRunners(
                    allGeneratedFeaturePaths, propertyManager.getDesiredNumberOfRunners()
            );
        }

        logger.logInfoSeparator(DEFAULT);
        logger.info(
                String.format("Cucable created %d separate %s and %d %s.",
                        featureFileCounter,
                        Language.singularPlural(featureFileCounter, "feature file", "feature files"),
                        runnerFileCounter,
                        Language.singularPlural(runnerFileCounter, "runner", "runners")
                ),
                DEFAULT, COMPACT, MINIMAL
        );
    }

    /**
     * Converts all scenarios in the given feature file to single
     * scenario feature files and their respective runners.
     *
     * @param sourceFeatureFilePath feature file to process.
     * @param lineNumbers           scenario line numbers.
     * @return Number of created scenarios.
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    private List<String> generateParallelizableFeatures(
            final Path sourceFeatureFilePath,
            final List<Integer> lineNumbers) throws CucablePluginException {

        if (propertyManager.getParallelizationMode() == PropertyManager.ParallelizationMode.FEATURES) {
            return generateFeaturesWithFeaturesParallelizationMode(sourceFeatureFilePath);
        }
        return generateFeaturesWithScenariosParallelizationMode(sourceFeatureFilePath, lineNumbers);
    }

    /**
     * Generate features with parallelization mode 'features'.
     *
     * @param sourceFeatureFilePath Path of the source feature(s).
     * @return A list of generated feature paths.
     * @throws CucablePluginException in case feature files cannot be created.
     */
    private List<String> generateFeaturesWithFeaturesParallelizationMode(final Path sourceFeatureFilePath) throws
            CucablePluginException {
        String featureFilePathString = sourceFeatureFilePath.toString();
        String featureFileContent = fileIO.readContentFromFile(featureFilePathString);
        return generateFeatureFiles(sourceFeatureFilePath, featureFileContent);
    }

    /**
     * Generate features with parallelization mode 'scenarios'.
     *
     * @param sourceFeatureFilePath path of the source feature(s).
     * @param lineNumbers           scenario line numbers.
     * @return A list of generated feature paths.
     * @throws CucablePluginException in case feature files cannot be created.
     */
    private List<String> generateFeaturesWithScenariosParallelizationMode(
            final Path sourceFeatureFilePath,
            final List<Integer> lineNumbers) throws CucablePluginException {

        String featureFilePathString = sourceFeatureFilePath.toString();
        if (featureFilePathString == null || featureFilePathString.equals("")) {
            throw new MissingFileException(featureFilePathString);
        }

        String featureFileContent = fileIO.readContentFromFile(featureFilePathString);

        List<SingleScenario> singleScenarios;
        singleScenarios =
                gherkinDocumentParser.getSingleScenariosFromFeature(
                        featureFileContent,
                        featureFilePathString,
                        lineNumbers
                );
        return generateFeatureFiles(sourceFeatureFilePath, singleScenarios);
    }

    /**
     * Write a single scenario feature to new files and return a list of generated file paths for later runner creation.
     *
     * @param sourceFeatureFilePath The complete path to the source feature.
     * @param singleScenarios       a list of single scenarios.
     * @return A list of generated feature file paths.
     * @throws FileCreationException Thrown if the feature file cannot be created.
     */
    private List<String> generateFeatureFiles(
            final Path sourceFeatureFilePath,
            final List<SingleScenario> singleScenarios) throws FileCreationException {

        // Stores all generated feature file names and associated source feature paths for later runner creation
        List<String> generatedFeaturePaths = new ArrayList<>();

        // Default parallelization mode
        for (SingleScenario singleScenario : singleScenarios) {
            String featureFileName = getFeatureFileNameFromPath(sourceFeatureFilePath);
            Integer featureCounter = singleFeatureCounters.getOrDefault(featureFileName, 0);
            featureCounter++;
            String scenarioCounterFilenamePart = String.format(SCENARIO_COUNTER_FORMAT, featureCounter);
            for (int testRuns = 1; testRuns <= propertyManager.getNumberOfTestRuns(); testRuns++) {
                String testRunsCounterFilenamePart = String.format(TEST_RUNS_COUNTER_FORMAT, testRuns);
                String generatedFileName =
                        featureFileName
                                .concat(scenarioCounterFilenamePart)
                                .concat(testRunsCounterFilenamePart)
                                .concat(INTEGRATION_TEST_POSTFIX);
                saveFeature(
                        generatedFileName,
                        featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)
                );
                generatedFeaturePaths.add(generatedFileName);
                singleFeatureCounters.put(featureFileName, featureCounter);
            }
        }

        logFeatureFileConversionMessage(sourceFeatureFilePath.toString(), singleScenarios.size());
        return generatedFeaturePaths;
    }

    /**
     * Write a copy of the original feature file content to new files and return a list of generated file paths for later runner creation.
     *
     * @param sourceFeatureFilePath The complete path to the source feature.
     * @param featureFileContent    the content to copy into the new feature file.
     * @return A list of generated feature file paths.
     * @throws FileCreationException Thrown if the feature file cannot be created.
     */
    private List<String> generateFeatureFiles(
            final Path sourceFeatureFilePath, final String featureFileContent) throws FileCreationException {

        // Stores all generated feature file names and associated source feature paths for later runner creation
        List<String> generatedFeaturePaths = new ArrayList<>();

        // Only parallelize complete features
        String featureFileName = getFeatureFileNameFromPath(sourceFeatureFilePath);
        Integer featureCounter = singleFeatureCounters.getOrDefault(featureFileName, 0);
        featureCounter++;
        String featureCounterFilenamePart = String.format(FEATURE_COUNTER_FORMAT, featureCounter);
        for (int testRuns = 1; testRuns <= propertyManager.getNumberOfTestRuns(); testRuns++) {
            String testRunsCounterFilenamePart = String.format(TEST_RUNS_COUNTER_FORMAT, testRuns);
            String generatedFileName =
                    featureFileName
                            .concat(featureCounterFilenamePart)
                            .concat(testRunsCounterFilenamePart)
                            .concat(INTEGRATION_TEST_POSTFIX);
            saveFeature(
                    generatedFileName,
                    featureFileContent
            );
            generatedFeaturePaths.add(generatedFileName);
            singleFeatureCounters.put(featureFileName, featureCounter);
        }

        logger.info(String.format("- processed %s.", featureFileName), DEFAULT);

        return generatedFeaturePaths;
    }

    /**
     * Save generated feature file contents and return the file path.
     *
     * @param featureFileName            The feature file name.
     * @param renderedFeatureFileContent The rendered file contents.
     * @throws FileCreationException if the file cannot be created.
     */
    private void saveFeature(final String featureFileName, final String renderedFeatureFileContent)
            throws FileCreationException {

        String generatedFeatureFilePath =
                propertyManager.getGeneratedFeatureDirectory()
                        .concat(PATH_SEPARATOR)
                        .concat(featureFileName)
                        .concat(FEATURE_FILE_EXTENSION);

        // Save scenario information to new feature file
        fileIO.writeContentToFile(renderedFeatureFileContent, generatedFeatureFilePath);
    }

    /**
     * Generate runner classes for a list of feature file paths.
     *
     * @param generatedFeatureNames  The list of generated feature file names.
     * @param numberOfDesiredRunners The number of desired runners (if set to 0, a runner is generated for each feature file path).
     * @return The number of generated runners.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    private int generateRunnerClassesWithDesiredNumberOfRunners(final List<String> generatedFeatureNames, final int numberOfDesiredRunners) throws
            CucablePluginException {

        int targetRunnerNumber = numberOfDesiredRunners;
        List<String> scenarioNames = propertyManager.getScenarioNames();

        if (targetRunnerNumber == 0) {
            targetRunnerNumber = generatedFeatureNames.size();
        }

        List<List<String>> generatedFeatureNamesPerRunner = new ArrayList<>(targetRunnerNumber);
        for (int i = 0; i < targetRunnerNumber; i++) {
            generatedFeatureNamesPerRunner.add(new ArrayList<>());
        }

        int currentRunnerIndex = 0;
        int matchCount = 0;
        for (String generatedFeatureName : generatedFeatureNames) {
            if (scenarioNames.isEmpty()) {
                generatedFeatureNamesPerRunner.get(currentRunnerIndex).add(generatedFeatureName);
                currentRunnerIndex++;
                if (currentRunnerIndex >= targetRunnerNumber) {
                    currentRunnerIndex = 0;
                }
            } else {
                // Move all scenarios matching a scenario name into its own group.
                String scenarioText = fileIO.readContentFromFile(propertyManager.getGeneratedFeatureDirectory() + "/" + generatedFeatureName + ".feature");
                if (scenarioText != null) {
                    Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
                    String language = parser.parse(scenarioText).getFeature().getLanguage();

                    int listIndex = gherkinDocumentParser.matchScenarioWithScenarioNames(language, scenarioText);
                    if (listIndex >= 0) {
                        generatedFeatureNamesPerRunner.get(listIndex).add(generatedFeatureName);
                        matchCount++;
                    }
                }
            }
        }

        if (!scenarioNames.isEmpty() && matchCount == 0) {
            throw new CucablePluginException(
                    "No matching scenarios found for specified scenario names - " + Arrays.toString(scenarioNames.toArray()) + "!"
            );
        }

        int runnerFileCounter = 0;
        for (List<String> generatedFeatureNamesForSingleRunner : generatedFeatureNamesPerRunner) {
            if (generatedFeatureNamesForSingleRunner.size() > 0) {
                generateRunnerClass(generatedFeatureNamesForSingleRunner);
                runnerFileCounter++;
            }
        }

        return runnerFileCounter;
    }

    /**
     * Generate runner classes for a list of feature file paths.
     *
     * @param generatedFeatureNames            The list of generated feature file names.
     * @param numberOfDesiredFeaturesPerRunner The number of desired features per runner.
     * @return The number of generated runners.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    private int generateRunnerClassesWithDesiredNumberOfFeatures(
            final List<String> generatedFeatureNames,
            final int numberOfDesiredFeaturesPerRunner)
            throws CucablePluginException {

        int currentRunnerFeatureCount = 0;
        int totalFeatureCount = 0;
        List<String> generatedFeatureNamesForSingleRunner = new ArrayList<>();
        int runnerFileCounter = 0;
        for (String generatedFeatureName : generatedFeatureNames) {
            generatedFeatureNamesForSingleRunner.add(generatedFeatureName);
            currentRunnerFeatureCount++;
            totalFeatureCount++;
            if (totalFeatureCount == generatedFeatureNames.size() ||
                    currentRunnerFeatureCount >= numberOfDesiredFeaturesPerRunner) {
                runnerFileCounter++;
                generateRunnerClass(generatedFeatureNamesForSingleRunner);
                currentRunnerFeatureCount = 0;
                generatedFeatureNamesForSingleRunner = new ArrayList<>();
            }
        }

        return runnerFileCounter;
    }


    /**
     * Generate a single runner class from a list of feature files.
     *
     * @param generatedFeatureFileNames The list of generated generated feature file names.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    private void generateRunnerClass(final List<String> generatedFeatureFileNames) throws CucablePluginException {
        // The runner class name will be equal to the feature name if there is only one feature to run.
        // Otherwise, a generated runner class name is used.
        String runnerClassName;
        if (generatedFeatureFileNames.size() == 1) {
            runnerClassName = generatedFeatureFileNames.get(0);
        } else {
            runnerClassName = "CucableMultiRunner_"
                    .concat(UUID.randomUUID().toString().replace("-", "_"))
                    .concat(INTEGRATION_TEST_POSTFIX);
        }

        // Generate runner for the newly generated single scenario feature file
        FeatureRunner featureRunner =
                new FeatureRunner(
                        propertyManager.getSourceRunnerTemplateFile(), runnerClassName, generatedFeatureFileNames);

        String renderedRunnerClassContent =
                runnerFileContentRenderer.getRenderedRunnerFileContent(featureRunner);

        String generatedRunnerClassFilePath =
                propertyManager.getGeneratedRunnerDirectory()
                        .concat(PATH_SEPARATOR)
                        .concat(runnerClassName)
                        .concat(RUNNER_FILE_EXTENSION);

        fileIO.writeContentToFile(renderedRunnerClassContent, generatedRunnerClassFilePath);
    }

    /**
     * Log the completion message for a feature file.
     *
     * @param featureFileName  The name of the processed feature file.
     * @param createdScenarios The number of created scenarios for the feature file.
     */
    private void logFeatureFileConversionMessage(
            final String featureFileName,
            final int createdScenarios) {

        String logPostfix = ".";
        logger.info(
                String.format("- generated %3d %s %s%s",
                        createdScenarios,
                        Language.singularPlural(createdScenarios, " scenario from", "scenarios from"),
                        featureFileName,
                        logPostfix
                ), DEFAULT);
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

