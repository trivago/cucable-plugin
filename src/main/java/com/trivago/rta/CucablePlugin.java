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

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.features.FeatureFileConverter;
import com.trivago.rta.files.FileManager;
import com.trivago.rta.properties.PropertyManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;

/**
 * The main plugin class.
 */
@Mojo(name = "parallel")
public final class CucablePlugin extends AbstractMojo {

    private final PropertyManager propertyManager;
    private final FileManager fileManager;
    private final FeatureFileConverter featureFileConverter;

    /**
     * The complete path to the runner template file.
     */
    @Parameter(property = "parallel.sourceRunnerTemplateFile", required = true)
    private String sourceRunnerTemplateFile = "";

    /**
     * The path where the generated runner classes should be created.
     */
    @Parameter(property = "parallel.generatedRunnerDirectory", required = true)
    private String generatedRunnerDirectory = "";

    /**
     * The path to .feature files or a concrete single feature file.
     */
    @Parameter(property = "parallel.sourceFeatures", required = true)
    private String sourceFeatures = "";

    /**
     * The path where the generated .feature files should be created.
     */
    @Parameter(property = "parallel.generatedFeatureDirectory", required = true)
    private String generatedFeatureDirectory = "";

    /**
     * An optional number of test runs for each generated .feature file.
     */
    @Parameter(property = "parallel.numberOfTestRuns", required = false, defaultValue = "1")
    private int numberOfTestRuns = 1;

    @Inject
    public CucablePlugin(
            PropertyManager propertyManager,
            FileManager fileManager,
            FeatureFileConverter featureFileConverter
    ) {
        this.propertyManager = propertyManager;
        this.fileManager = fileManager;
        this.featureFileConverter = featureFileConverter;
    }

    /**
     * Cucable start method.
     *
     * @throws CucablePluginException When thrown, the plugin execution is stopped.
     */
    public void execute() throws CucablePluginException {

        // Initialize and validate passed pom properties
        propertyManager.setSourceRunnerTemplateFile(sourceRunnerTemplateFile);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDirectory);
        propertyManager.setSourceFeatures(sourceFeatures);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDirectory);
        propertyManager.setNumberOfTestRuns(numberOfTestRuns);

        getLog().info(propertyManager.toString());
        propertyManager.validateSettings();

        fileManager.prepareGeneratedFeatureAndRunnerDirs();

        getLog().info("Cucable - starting conversion...");
        int numberOfProcessedFeatureFiles =
                featureFileConverter.convertToSingleScenariosAndRunners(
                        fileManager.getFeatureFilePaths()
                );

        getLog().info("Cucable - finished processing "
                + numberOfProcessedFeatureFiles + " feature file(s)!");
    }
}



