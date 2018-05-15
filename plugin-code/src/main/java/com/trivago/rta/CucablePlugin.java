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

package com.trivago.rta;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.features.FeatureFileConverter;
import com.trivago.rta.files.FileSystemManager;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.util.List;

/**
 * The main plugin class.
 */
@Mojo(name = "parallel")
final class CucablePlugin extends AbstractMojo {

    private final PropertyManager propertyManager;
    private final FileSystemManager fileManager;
    private final FeatureFileConverter featureFileConverter;
    private final CucableLogger logger;

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
    @Parameter(property = "parallel.numberOfTestRuns", defaultValue = "1")
    private int numberOfTestRuns = 1;

    /**
     * Optional scenario tags to be included from feature and runner generation.
     * If used together with excludeScenarioTags, the excluded tags overrule this setting.
     */
    @Parameter(property = "parallel.includeScenarioTags")
    private List<String> includeScenarioTags;

    /**
     * Optional scenario tags to be excluded from feature and runner generation
     * If used together with includeScenarioTags, the excluded tags overrule the included ones.
     */
    @Parameter(property = "parallel.excludeScenarioTags")
    private List<String> excludeScenarioTags;

    /**
     * Optional fixed number of test runners that each run multiple scenarios in sequence.
     */
    @Parameter(property = "parallel.fixedNumberOfRunners", defaultValue = "0")
    private int fixedNumberOfRunners = 0;

    /**
     * Optional log level to control what information is logged in the console.
     * Allowed values: default, compact, minimal, off
     */
    @Parameter(property = "parallel.logLevel")
    private String logLevel = "default";

    @Inject
    public CucablePlugin(
            PropertyManager propertyManager,
            FileSystemManager fileManager,
            FeatureFileConverter featureFileConverter,
            CucableLogger logger
    ) {
        this.propertyManager = propertyManager;
        this.fileManager = fileManager;
        this.featureFileConverter = featureFileConverter;
        this.logger = logger;
    }

    /**
     * Cucable start method.
     *
     * @throws CucablePluginException When thrown, the plugin execution is stopped.
     */
    public void execute() throws CucablePluginException {

        // Initialize logger to be available outside the AbstractMojo class
        logger.initialize(getLog(), logLevel);

        // Initialize and validate passed pom properties
        propertyManager.setSourceRunnerTemplateFile(sourceRunnerTemplateFile);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDirectory);
        propertyManager.setSourceFeatures(sourceFeatures);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDirectory);
        propertyManager.setNumberOfTestRuns(numberOfTestRuns);
        propertyManager.setExcludeScenarioTags(excludeScenarioTags);
        propertyManager.setIncludeScenarioTags(includeScenarioTags);
        propertyManager.setFixedNumberOfRunners(fixedNumberOfRunners);
        propertyManager.validateSettings();

        // Logging
        logHeader();
        propertyManager.logProperties();

        // Create the necessary directories if missing.
        fileManager.prepareGeneratedFeatureAndRunnerDirs();

        // Conversion of scenarios into single scenarios and runners.
        featureFileConverter.convertToSingleScenariosAndRunners(fileManager.getFeatureFilePaths());
    }

    /**
     * Log the plugin name and version.
     */
    private void logHeader() {
        CucableLogger.CucableLogLevel[] cucableLogLevels =
                new CucableLogger.CucableLogLevel[]{CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT};
        logger.info("-------------------------------------", cucableLogLevels);
        logger.info(String.format(" Cucable Maven Plugin, version %s", getClass().getPackage().getImplementationVersion()), cucableLogLevels);
        logger.info("-------------------------------------", cucableLogLevels);
    }
}



