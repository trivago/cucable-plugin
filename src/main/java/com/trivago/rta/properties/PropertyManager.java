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
import com.trivago.rta.exceptions.MissingFileException;
import com.trivago.rta.exceptions.MissingPropertyException;

import javax.inject.Singleton;
import java.io.File;

@Singleton
public class PropertyManager {
    // Generated source runner template file placeholder for logging.
    private static final String
            SOURCE_RUNNER_TEMPLATE_FILE = "<sourceRunnerTemplateFile>";

    // Generated runner directory placeholder for logging.
    private static final String
            GENERATED_RUNNER_DIRECTORY = "<generatedRunnerDirectory>";

    // Source feature directory placeholder for logging.
    private static final String
            SOURCE_FEATURE_DIRECTORY = "<sourceFeatureDirectory>";

    // Generated feature directory placeholder for logging.
    private static final String
            GENERATED_FEATURE_DIRECTORY = "<generatedFeatureDirectory>";

    private String sourceRunnerTemplateFile;
    private String generatedRunnerDirectory;
    private String sourceFeatureDirectory;
    private String generatedFeatureDirectory;

    public String getSourceRunnerTemplateFile() {
        return sourceRunnerTemplateFile;
    }

    public String getGeneratedRunnerDirectory() {
        return generatedRunnerDirectory;
    }

    public String getSourceFeatureDirectory() {
        return sourceFeatureDirectory;
    }

    public String getGeneratedFeatureDirectory() {
        return generatedFeatureDirectory;
    }

    public void setSourceRunnerTemplateFile(final String sourceRunnerTemplateFile) {
        this.sourceRunnerTemplateFile = sourceRunnerTemplateFile;
    }

    public void setGeneratedRunnerDirectory(final String generatedRunnerDirectory) {
        this.generatedRunnerDirectory = generatedRunnerDirectory;
    }

    public void setSourceFeatureDirectory(final String sourceFeatureDirectory) {
        this.sourceFeatureDirectory = sourceFeatureDirectory;
    }

    public void setGeneratedFeatureDirectory(final String generatedFeatureDirectory) {
        this.generatedFeatureDirectory = generatedFeatureDirectory;
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

        if (generatedFeatureDirectory.equals("")) {
            throw new MissingPropertyException(SOURCE_FEATURE_DIRECTORY);
        }

        if (generatedFeatureDirectory.equals("")) {
            throw new MissingPropertyException(GENERATED_FEATURE_DIRECTORY);
        }

        // Runner template file
        File runnerTemplateFile = new File(sourceRunnerTemplateFile);
        if (!runnerTemplateFile.exists()) {
            throw new MissingFileException(sourceRunnerTemplateFile);
        }

        // SingleScenarioFeature directory
        File featureDirectory = new File(sourceFeatureDirectory);
        if (!featureDirectory.exists() || !featureDirectory.isDirectory()) {
            throw new CucablePluginException(
                    SOURCE_FEATURE_DIRECTORY
                            + " does not exist or is not a directory: "
                            + sourceFeatureDirectory
            );
        }
    }

    @Override
    public String toString() {
        String lineFeed = System.lineSeparator();
        return "Cucable properties:" + lineFeed +
                "       - sourceRunnerTemplateFile  : " + sourceRunnerTemplateFile + lineFeed +
                "       - generatedRunnerDirectory  : " + generatedRunnerDirectory + lineFeed +
                "       - sourceFeatureDirectory    : " + sourceFeatureDirectory + lineFeed +
                "       - generatedFeatureDirectory : " + generatedFeatureDirectory;
    }
}
