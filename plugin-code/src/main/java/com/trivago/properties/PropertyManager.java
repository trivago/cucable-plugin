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

package com.trivago.properties;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.properties.WrongOrMissingPropertiesException;
import com.trivago.logging.CucableLogger;
import com.trivago.logging.CucableLogger.CucableLogLevel;
import com.trivago.logging.Language;
import com.trivago.vo.CucableFeature;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.trivago.logging.CucableLogger.CucableLogLevel.COMPACT;
import static com.trivago.logging.CucableLogger.CucableLogLevel.DEFAULT;

@Singleton
public class PropertyManager {

    private final CucableLogger logger;

    private String sourceRunnerTemplateFile;
    private String generatedRunnerDirectory;
    private List<CucableFeature> sourceFeatures;
    private String generatedFeatureDirectory;
    private int numberOfTestRuns;
    private String includeScenarioTags;
    private ParallelizationMode parallelizationMode;
    private Map<String, String> customPlaceholders;
    private int desiredNumberOfRunners;
    private int desiredNumberOfFeaturesPerRunner;
    private List<String> scenarioNames = new ArrayList<>();

    @Inject
    public PropertyManager(CucableLogger logger) {
        this.logger = logger;
    }

    public String getSourceRunnerTemplateFile() {
        return sourceRunnerTemplateFile;
    }

    public void setSourceRunnerTemplateFile(final String sourceRunnerTemplateFile) {
        this.sourceRunnerTemplateFile = sourceRunnerTemplateFile;
    }

    public String getGeneratedRunnerDirectory() {
        return generatedRunnerDirectory;
    }

    public void setGeneratedRunnerDirectory(final String generatedRunnerDirectory) {
        this.generatedRunnerDirectory = generatedRunnerDirectory;
    }

    public List<CucableFeature> getSourceFeatures() {
        return sourceFeatures;
    }

    public void setSourceFeatures(final String sourceFeatures) {
        List<CucableFeature> cucableFeatures = new ArrayList<>();
        Pattern lineNumberPattern = Pattern.compile(":(\\d*)");
        for (String sourceFeature : sourceFeatures.split(",")) {
            String trimmedFeature = sourceFeature.trim();
            StringBuffer resultBuffer = new StringBuffer();
            Matcher matcher = lineNumberPattern.matcher(trimmedFeature);
            List<Integer> scenarioLineNumbers = new ArrayList<>();
            while (matcher.find()) {
                try {
                    scenarioLineNumbers.add(Integer.parseInt(matcher.group(1)));
                    matcher.appendReplacement(resultBuffer, "");
                } catch (NumberFormatException ignored) {
                    // Ignore unparsable line numbers
                }
            }
            matcher.appendTail(resultBuffer);
            cucableFeatures.add(new CucableFeature(resultBuffer.toString(), scenarioLineNumbers));
        }

        this.sourceFeatures = cucableFeatures;
    }

    public String getGeneratedFeatureDirectory() {
        return generatedFeatureDirectory;
    }

    public void setGeneratedFeatureDirectory(final String generatedFeatureDirectory) {
        this.generatedFeatureDirectory = generatedFeatureDirectory;
    }

    public int getNumberOfTestRuns() {
        return numberOfTestRuns;
    }

    public void setNumberOfTestRuns(final int numberOfTestRuns) {
        this.numberOfTestRuns = numberOfTestRuns;
    }

    public String getIncludeScenarioTags() {
        return includeScenarioTags;
    }

    public void setIncludeScenarioTags(final String includeScenarioTags) {
        this.includeScenarioTags = includeScenarioTags;
    }

    public ParallelizationMode getParallelizationMode() {
        return parallelizationMode;
    }

    public void setParallelizationMode(final String parallelizationMode) throws CucablePluginException {
        try {
            this.parallelizationMode = ParallelizationMode.valueOf(parallelizationMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CucablePluginException(
                    "Unknown <parallelizationMode> '" + parallelizationMode +
                            "'. Please use 'scenarios' or 'features'."
            );
        }
    }

    public Map<String, String> getCustomPlaceholders() {
        return customPlaceholders;
    }

    public void setCustomPlaceholders(final Map<String, String> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
    }

    public int getDesiredNumberOfRunners() {
        return desiredNumberOfRunners;
    }

    public void setDesiredNumberOfRunners(final int desiredNumberOfRunners) {
        this.desiredNumberOfRunners = desiredNumberOfRunners;
    }

    public int getDesiredNumberOfFeaturesPerRunner() {
        return desiredNumberOfFeaturesPerRunner;
    }

    public void setDesiredNumberOfFeaturesPerRunner(int desiredNumberOfFeaturesPerRunner) {
        this.desiredNumberOfFeaturesPerRunner = desiredNumberOfFeaturesPerRunner;
    }

    public List<String> getScenarioNames() {
        return scenarioNames;
    }

    public void setScenarioNames(final String scenarioNames) {
        List<String> scenarioNameList = new ArrayList<>();

        if (scenarioNames != null && !scenarioNames.trim().isEmpty()) {
            // Split scenarioNames on ',' and adjacent spaces to avoid multiple trims.
            scenarioNameList = Arrays.asList(scenarioNames.trim().split("\\s*,\\s*"));

            // If scenarioNames is specified, set desiredNumberOfRunners to the number of scenario names.
            setDesiredNumberOfRunners(scenarioNameList.size());
        }

        this.scenarioNames = scenarioNameList;
    }

    /**
     * Checks the pom settings for the plugin.
     *
     * @throws CucablePluginException Thrown when a required setting
     *                                is not specified in the pom.
     */
    public void checkForMissingMandatoryProperties() throws CucablePluginException {
        List<String> missingProperties = new ArrayList<>();

        if (sourceFeatures == null || sourceFeatures.isEmpty()) {
            saveMissingProperty("", "<sourceFeatures>", missingProperties);
        }
        saveMissingProperty(sourceRunnerTemplateFile, "<sourceRunnerTemplateFile>", missingProperties);
        saveMissingProperty(generatedRunnerDirectory, "<generatedRunnerDirectory>", missingProperties);
        saveMissingProperty(generatedFeatureDirectory, "<generatedFeatureDirectory>", missingProperties);

        if (!missingProperties.isEmpty()) {
            throw new WrongOrMissingPropertiesException(missingProperties);
        }
    }

    /**
     * Checks for properties that are not allowed in combination.
     *
     * @throws CucablePluginException Thrown when a wrong property is used.
     */
    public void checkForDisallowedPropertyCombinations() throws CucablePluginException {

        if (desiredNumberOfFeaturesPerRunner > 0 && desiredNumberOfRunners > 0) {
            throw new CucablePluginException(
                    "You cannot use desiredNumberOfFeaturesPerRunner and desiredNumberOfRunners/scenarioNames at the same time!"
            );
        }

        if (parallelizationMode == ParallelizationMode.SCENARIOS) {
            return;
        }

        String errorMessage = "";
        if (!new File(String.valueOf(sourceFeatures.get(0).getName())).isDirectory()) {
            errorMessage = "sourceFeatures should point to a directory!";
        } else if (includeScenarioTags != null && !includeScenarioTags.isEmpty()) {
            errorMessage = "you cannot specify includeScenarioTags!";
        } else if (scenarioNames != null && !scenarioNames.isEmpty()) {
            errorMessage = "you cannot specify scenarioNames!";
        }
        if (!errorMessage.isEmpty()) {
            throw new CucablePluginException("In parallelizationMode = FEATURE, ".concat(errorMessage));
        }
    }

    /**
     * Logs all passed property values.
     */
    public void logProperties() {
        CucableLogLevel[] logLevels = new CucableLogLevel[]{DEFAULT, COMPACT};

        logger.info("- sourceFeatures               :", logLevels);
        if (sourceFeatures != null) {
            for (CucableFeature sourceFeature : sourceFeatures) {
                String logLine = "  - " + sourceFeature.getName();
                if (sourceFeature.hasValidScenarioLineNumbers()) {
                    List<Integer> lineNumbers = sourceFeature.getLineNumbers();
                    logLine += String.format(" with %s %s",
                            Language.singularPlural(lineNumbers.size(), "line number", "line numbers"),
                            lineNumbers.stream().map(String::valueOf).collect(Collectors.joining(",")));
                }
                logger.info(logLine, logLevels);
            }
        }

        logger.info(String.format("- sourceRunnerTemplateFile     : %s", sourceRunnerTemplateFile), logLevels);

        logger.logInfoSeparator(DEFAULT);
        logger.info(String.format("- generatedRunnerDirectory     : %s", generatedRunnerDirectory), logLevels);
        logger.info(String.format("- generatedFeatureDirectory    : %s", generatedFeatureDirectory), logLevels);
        logger.logInfoSeparator(DEFAULT);

        if (includeScenarioTags != null && !includeScenarioTags.isEmpty()) {
            logger.info(String.format("- includeScenarioTags          : %s",
                    String.join(", ", includeScenarioTags)), logLevels);
        }
        if (customPlaceholders != null && !customPlaceholders.isEmpty()) {
            logger.info("- customPlaceholders           :", logLevels);
            for (Map.Entry<String, String> customPlaceholder : customPlaceholders.entrySet()) {
                logger.info(
                        String.format("  %s => %s", customPlaceholder.getKey(), customPlaceholder.getValue()),
                        logLevels
                );
            }
        }

        logger.info(String.format("- parallelizationMode          : %s", parallelizationMode.name().toLowerCase()), logLevels);
        logger.info(String.format("- numberOfTestRuns             : %d", numberOfTestRuns), logLevels);

        if (desiredNumberOfRunners > 0) {
            logger.info(String.format("- desiredNumberOfRunners       : %d", desiredNumberOfRunners), logLevels);
        }

        logger.logInfoSeparator(logLevels);
    }

    /**
     * Checks if a property is null or empty and adds it to the missingProperties list.
     *
     * @param propertyValue     The value of the property to check.
     * @param propertyName      The name of the property to check.
     * @param missingProperties The list of missing properties.
     */
    private void saveMissingProperty(
            final String propertyValue,
            final String propertyName,
            final List<String> missingProperties
    ) {
        if (propertyValue == null || propertyValue.isEmpty()) {
            missingProperties.add(propertyName);
        }
    }

    public enum ParallelizationMode {
        SCENARIOS, FEATURES
    }
}
