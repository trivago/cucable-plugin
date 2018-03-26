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
import com.trivago.rta.exceptions.properties.WrongOrMissingPropertyException;
import com.trivago.rta.logging.CucableLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class PropertyManager {
    // Generated source runner template file placeholder for logging.
    private static final String SOURCE_RUNNER_TEMPLATE_FILE = "<sourceRunnerTemplateFile>";

    // Generated runner directory placeholder for logging.
    private static final String GENERATED_RUNNER_DIRECTORY = "<generatedRunnerDirectory>";

    // Source features placeholder for logging.
    private static final String SOURCE_FEATURES = "<sourceFeatures>";

    // Generated feature directory placeholder for logging.
    private static final String GENERATED_FEATURE_DIRECTORY = "<generatedFeatureDirectory>";

    private final CucableLogger logger;

    private String sourceRunnerTemplateFile;
    private String generatedRunnerDirectory;
    private String sourceFeatures;
    private String generatedFeatureDirectory;
    private List<Integer> scenarioLineNumbers;
    private int numberOfTestRuns;
    private List<String> includeScenarioTags;
    private List<String> excludeScenarioTags;
    private String logLevel;

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

    public void setExcludeScenarioTags(final List<String> excludeScenarioTags) {
        this.excludeScenarioTags = excludeScenarioTags;
    }

    public List<String> getIncludeScenarioTags() {
        return includeScenarioTags;
    }

    public void setIncludeScenarioTags(final List<String> includeScenarioTags) {
        this.includeScenarioTags = includeScenarioTags;
    }

    public void setLogLevel(final String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() {
        return logLevel;
    }

    /**
     * Checks the pom settings for the plugin.
     *
     * @throws CucablePluginException Thrown when a required setting
     *                                is not specified in the pom.
     */
    public void validateSettings() throws CucablePluginException {
        String missingProperty = null;
        if (sourceRunnerTemplateFile == null || sourceRunnerTemplateFile.equals("")) {
            missingProperty = SOURCE_RUNNER_TEMPLATE_FILE;
        } else if (generatedRunnerDirectory == null || generatedRunnerDirectory.equals("")) {
            missingProperty = GENERATED_RUNNER_DIRECTORY;
        } else if (sourceFeatures == null || sourceFeatures.equals("")) {
            missingProperty = SOURCE_FEATURES;
        } else if (generatedFeatureDirectory == null || generatedFeatureDirectory.equals("")) {
            missingProperty = GENERATED_FEATURE_DIRECTORY;
        }

        if (missingProperty != null) {
            throw new WrongOrMissingPropertyException(missingProperty);
        }

        if (includeScenarioTags != null) {
            for (String includeTag : includeScenarioTags) {
                if (!includeTag.startsWith("@")) {
                    throw new CucablePluginException("Include tag '" + includeTag + "' does not start with '@'.");
                }
            }
        } else if (excludeScenarioTags != null) {
            for (String excludeTag : excludeScenarioTags) {
                if (!excludeTag.startsWith("@")) {
                    throw new CucablePluginException("Exclude tag '" + excludeTag + "' does not start with '@'.");
                }
            }
        }
    }

    /**
     * Logs all passed property values.
     */
    public void logProperties() {
        logger.info(String.format("- sourceRunnerTemplateFile  : %s", sourceRunnerTemplateFile));
        logger.info(String.format("- generatedRunnerDirectory  : %s", generatedRunnerDirectory));

        logger.info(String.format("- sourceFeature(s)          : %s", sourceFeatures));
        if (hasValidScenarioLineNumbers()) {
            logger.info(String.format("%30swith line number(s) %s", " ", scenarioLineNumbers));
        }

        if (includeScenarioTags != null) {
            logger.info(String.format("- include scenario tag(s)   : %s", String.join(", ", includeScenarioTags)));
        }
        if (excludeScenarioTags != null) {
            logger.info(String.format("- exclude scenario tag(s)   : %s", String.join(", ", excludeScenarioTags)));
        }

        logger.info(String.format("- generatedFeatureDirectory : %s", generatedFeatureDirectory));
        logger.info(String.format("- numberOfTestRuns          : %d", numberOfTestRuns));
    }
}
