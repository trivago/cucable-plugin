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

package com.trivago.rta;

import com.trivago.rta.exceptions.MissingFileException;
import com.trivago.rta.exceptions.MissingPropertyException;
import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.feature.FeatureFileConverter;
import com.trivago.rta.utils.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * The main plugin class.
 */
@Mojo(name = "parallel")
public final class CucablePlugin extends AbstractMojo {

    /**
     * Generated source runner template file placeholder for logging.
     */
    private static final String
            SOURCE_RUNNER_TEMPLATE_FILE = "<sourceRunnerTemplateFile>";
    /**
     * Generated runner directory placeholder for logging.
     */
    private static final String
            GENERATED_RUNNER_DIRECTORY = "<generatedRunnerDirectory>";
    /**
     * Source feature directory placeholder for logging.
     */
    private static final String
            SOURCE_FEATURE_DIRECTORY = "<sourceFeatureDirectory>";
    /**
     * Generated feature directory placeholder for logging.
     */
    private static final String
            GENERATED_FEATURE_DIRECTORY = "<generatedFeatureDirectory>";
    /**
     * Automatically filled source runner template file property from the pom.
     */
    @Parameter(property = "parallel.sourceRunnerTemplateFile", required = true)
    private String sourceRunnerTemplateFile = "";
    /**
     * Automatically filled generated runner directory property from the pom.
     */
    @Parameter(property = "parallel.generatedRunnerDirectory", required = true)
    private String generatedRunnerDirectory = "";
    /**
     * Automatically filled source feature directory property from the pom.
     */
    @Parameter(property = "parallel.sourceFeatureDirectory", required = true)
    private String sourceFeatureDirectory = "";
    /**
     * Automatically filled generated feature directory property from the pom.
     */
    @Parameter(property = "parallel.generatedFeatureDirectory", required = true)
    private String generatedFeatureDirectory = "";

    /**
     * Standard mojo main method.
     *
     * @throws CucablePluginException When thrown,
     *                              the plugin execution is stopped.
     */
    public void execute() throws CucablePluginException {
        validatePluginPomSettings();
        clearTempDirectories();

        int counter = 0;
        FeatureFileConverter featureFileConverter = new FeatureFileConverter();
        List<Path> featureFilePaths =
                FileUtils.getFeatureFilePaths(sourceFeatureDirectory);
        for (Path featureFileLocation : featureFilePaths) {
            featureFileConverter.convertToSingleScenariosAndRunners(
                    featureFileLocation,
                    generatedFeatureDirectory,
                    generatedRunnerDirectory,
                    sourceRunnerTemplateFile
            );
            counter++;
        }
        getLog().info("Finished processing "
                + counter + " feature file(s).");
    }

    /**
     * Checks the pom settings for the plugin.
     *
     * @throws CucablePluginException Thrown when a required setting
     *                              is not specified in the pom.
     */
    private void validatePluginPomSettings() throws CucablePluginException {
        if (sourceRunnerTemplateFile.equals("")) {
            throw new MissingPropertyException(SOURCE_RUNNER_TEMPLATE_FILE);
        }

        if (generatedRunnerDirectory.equals("")) {
            throw new MissingPropertyException(GENERATED_RUNNER_DIRECTORY);
        }

        if (sourceFeatureDirectory.equals("")) {
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

    /**
     * Removes all files from the specified generated
     * feature directory and generated runner directory.
     */
    private void clearTempDirectories() {
        FileUtils.removeFilesFromPath(generatedFeatureDirectory, "feature");
        FileUtils.removeFilesFromPath(generatedRunnerDirectory, "java");
    }
}



