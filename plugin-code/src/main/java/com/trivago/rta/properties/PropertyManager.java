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

package com.trivago.rta.properties;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.properties.WrongOrMissingPropertiesException;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.logging.CucableLogger.CucableLogLevel;
import com.trivago.rta.logging.Language;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.trivago.rta.logging.CucableLogger.CucableLogLevel.COMPACT;
import static com.trivago.rta.logging.CucableLogger.CucableLogLevel.DEFAULT;

@Singleton
public class PropertyManager {

    private final CucableLogger logger;

    private String sourceRunnerTemplateFile;
    private String generatedRunnerDirectory;
    private String sourceFeatures;
    private String generatedFeatureDirectory;
    private List<Integer> scenarioLineNumbers;
    private int numberOfTestRuns;
    private List<String> includeScenarioTags;
    private List<String> excludeScenarioTags;
    private ParallelizationMode parallelizationMode;
    private Map<String, String> customPlaceholders;
    private int desiredNumberOfRunners;

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

    public String getSourceFeatures() {
        return sourceFeatures;
    }

    public void setSourceFeatures(final String sourceFeatures) {
        StringBuffer resultBuffer = new StringBuffer();
        Matcher matcher = Pattern.compile(":(\\d*)").matcher(sourceFeatures);
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
        this.scenarioLineNumbers = scenarioLineNumbers;
        this.sourceFeatures = resultBuffer.toString();
    }

    public List<Integer> getScenarioLineNumbers() {
        return scenarioLineNumbers;
    }

    public boolean hasValidScenarioLineNumbers() {
        return scenarioLineNumbers != null && !scenarioLineNumbers.isEmpty();
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

    public List<String> getExcludeScenarioTags() {
        return excludeScenarioTags;
    }

    public void setExcludeScenarioTags(final List<String> excludeScenarioTags) throws CucablePluginException {
        this.excludeScenarioTags = excludeScenarioTags;
        validateTags(excludeScenarioTags, "exclude");
    }

    public List<String> getIncludeScenarioTags() {
        return includeScenarioTags;
    }

    public void setIncludeScenarioTags(final List<String> includeScenarioTags) throws CucablePluginException {
        this.includeScenarioTags = includeScenarioTags;
        validateTags(includeScenarioTags, "include");
    }

    public ParallelizationMode getParallelizationMode() {
        return parallelizationMode;
    }

    public void setParallelizationMode(final String parallelizationMode) throws CucablePluginException {
        try {
            this.parallelizationMode = ParallelizationMode.valueOf(parallelizationMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CucablePluginException(
                    "Unknown <parallelizationMode> '" + parallelizationMode + "'. Please use 'scenarios' or 'features'."
            );
        }
    }

    public void setCustomPlaceholders(final Map<String, String> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
    }

    public Map<String, String> getCustomPlaceholders() {
        return customPlaceholders;
    }

    public int getDesiredNumberOfRunners() {
        return desiredNumberOfRunners;
    }

    public void setDesiredNumberOfRunners(final int desiredNumberOfRunners) {
        this.desiredNumberOfRunners = desiredNumberOfRunners;
    }

    /**
     * Checks the pom settings for the plugin.
     *
     * @throws CucablePluginException Thrown when a required setting
     *                                is not specified in the pom.
     */
    public void checkForMissingMandatoryProperties() throws CucablePluginException {
        List<String> missingProperties = new ArrayList<>();

        saveMissingProperty(sourceRunnerTemplateFile, "<sourceRunnerTemplateFile>", missingProperties);
        saveMissingProperty(generatedRunnerDirectory, "<generatedRunnerDirectory>", missingProperties);
        saveMissingProperty(sourceFeatures, "<sourceFeatures>", missingProperties);
        saveMissingProperty(generatedFeatureDirectory, "<generatedFeatureDirectory>", missingProperties);

        if (!missingProperties.isEmpty()) {
            throw new WrongOrMissingPropertiesException(missingProperties);
        }
    }

    /**
     * Checks for properties that are not allowed in parallelizationMode = FEATURE.
     *
     * @throws CucablePluginException Thrown when a wrong property is used.
     */
    public void checkForDisallowedParallelizationModeProperties() throws CucablePluginException {
        if (parallelizationMode == ParallelizationMode.SCENARIOS) {
            return;
        }
        String errorMessage = "";

        if (!new File(sourceFeatures).isDirectory()) {
            errorMessage = "sourceFeatures should point to a directory!";
        } else if (excludeScenarioTags != null && !excludeScenarioTags.isEmpty()) {
            errorMessage = "you cannot specify excludeScenarioTags!";
        } else if (includeScenarioTags != null && !includeScenarioTags.isEmpty()) {
            errorMessage = "you cannot specify includeScenarioTags!";
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

        logger.info(String.format("- sourceRunnerTemplateFile  : %s", sourceRunnerTemplateFile), logLevels);
        logger.info(String.format("- generatedRunnerDirectory  : %s", generatedRunnerDirectory), logLevels);

        logger.info(String.format("- sourceFeatures            : %s", sourceFeatures), logLevels);
        if (hasValidScenarioLineNumbers()) {
            logger.info(String.format("%30swith %s %s", " ",
                    Language.singularPlural(scenarioLineNumbers.size(), "line number", "line numbers"),
                    scenarioLineNumbers.stream().map(String::valueOf).collect(Collectors.joining(", "))),
                    logLevels
            );
        }

        if (includeScenarioTags != null && !includeScenarioTags.isEmpty()) {
            logger.info(String.format("- includeScenarioTags       : %s",
                    String.join(", ", includeScenarioTags)), logLevels);
        }
        if (excludeScenarioTags != null && !excludeScenarioTags.isEmpty()) {
            logger.info(String.format("- excludeScenarioTags       : %s",
                    String.join(", ", excludeScenarioTags)), logLevels);
        }

        logger.info(String.format("- parallelizationMode       : %s", parallelizationMode), logLevels);

        if (customPlaceholders != null && !customPlaceholders.isEmpty()) {
            logger.info("- customPlaceholders        :", logLevels);
            for (Map.Entry<String, String> customPlaceholder : customPlaceholders.entrySet()) {
                logger.info(
                        String.format("  %s => %s", customPlaceholder.getKey(), customPlaceholder.getValue()),
                        logLevels
                );
            }
        }

        logger.info(String.format("- generatedFeatureDirectory : %s", generatedFeatureDirectory), logLevels);
        logger.info(String.format("- numberOfTestRuns          : %d", numberOfTestRuns), logLevels);

        if (desiredNumberOfRunners > 0) {
            logger.info(String.format("- desiredNumberOfRunners    : %d", desiredNumberOfRunners), logLevels);
        }

        logger.logInfoSeparator(logLevels);
    }

    /**
     * Checks a list of tags for missing '@' prefixes
     *
     * @param tags    A list of tags.
     * @param tagType The type of the passed tags.
     * @throws CucablePluginException Thrown when a tag does not start with '@'.
     */
    private void validateTags(final List<String> tags, final String tagType) throws CucablePluginException {
        if (tags != null) {
            for (String tag : tags) {
                if (!tag.startsWith("@")) {
                    throw new CucablePluginException(
                            "Tag '" + tag + "' of type '" + tagType + "' does not start with '@'.");
                }
            }
        }
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
