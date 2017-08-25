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

package com.trivago.rta.properties;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.properties.MissingPropertyException;

import javax.inject.Singleton;

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

    // Number of test runs.
    private static final String NUMBER_OF_TEST_RUNS = "<numberOfTestRuns>";

    private String sourceRunnerTemplateFile;
    private String generatedRunnerDirectory;
    private String sourceFeatures;
    private String generatedFeatureDirectory;
    private int numberOfTestRuns;

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
        this.sourceFeatures = sourceFeatures;
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

    /**
     * Checks the pom settings for the plugin.
     *
     * @throws CucablePluginException Thrown when a required setting
     *                                is not specified in the pom.
     */
    public void validateSettings() throws CucablePluginException {
        if (sourceRunnerTemplateFile.equals("")) {
            throw new MissingPropertyException(SOURCE_RUNNER_TEMPLATE_FILE);
        }

        if (generatedRunnerDirectory.equals("")) {
            throw new MissingPropertyException(GENERATED_RUNNER_DIRECTORY);
        }

        if (sourceFeatures.equals("")) {
            throw new MissingPropertyException(SOURCE_FEATURES);
        }

        if (generatedFeatureDirectory.equals("")) {
            throw new MissingPropertyException(GENERATED_FEATURE_DIRECTORY);
        }
    }

    @Override
    public String toString() {
        String lineFeed = System.lineSeparator();
        return "Cucable properties:" + lineFeed +
                "       - sourceRunnerTemplateFile  : " + sourceRunnerTemplateFile + lineFeed +
                "       - generatedRunnerDirectory  : " + generatedRunnerDirectory + lineFeed +
                "       - sourceFeatures            : " + sourceFeatures + lineFeed +
                "       - generatedFeatureDirectory : " + generatedFeatureDirectory + lineFeed +
                "       - numberOfTestRuns          : " + numberOfTestRuns;
    }
}
