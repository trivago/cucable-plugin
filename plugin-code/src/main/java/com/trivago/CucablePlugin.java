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

package com.trivago;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.features.FeatureFileConverter;
import com.trivago.files.FileSystemManager;
import com.trivago.logging.CucableLogger;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.CucableFeature;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.inject.Inject;
import java.util.Map;

/**
 * The main plugin class.
 */
@SuppressWarnings("unused")
@Mojo(name = "parallel")
final class
CucablePlugin extends AbstractMojo {

    private final PropertyManager propertyManager;
    private final FileSystemManager fileManager;
    private final FeatureFileConverter featureFileConverter;
    private final CucableLogger logger;

    /**
     * The complete path to the runner template file.
     */
    @Parameter(property = "parallel.sourceRunnerTemplateFile", required = false, defaultValue = "")
    private String sourceRunnerTemplateFile;

    /**
     * The path where the generated runner classes should be created.
     */
    @Parameter(property = "parallel.generatedRunnerDirectory", required = false, defaultValue = "")
    private String generatedRunnerDirectory;

    /**
     * The path to .feature files or a concrete single feature file.
     */
    @Parameter(property = "parallel.sourceFeatures", required = true)
    private String sourceFeatures;

    /**
     * The path where the generated .feature files should be created.
     */
    @Parameter(property = "parallel.generatedFeatureDirectory", required = true)
    private String generatedFeatureDirectory;

    /**
     * An optional number of test runs for each generated .feature file.
     */
    @Parameter(property = "parallel.numberOfTestRuns", defaultValue = "1")
    private int numberOfTestRuns;

    /**
     * Optional Cucumber tag expression to include or exclude certain tagged scenarios.
     * See also <a href="https://docs.cucumber.io/cucumber/api/#tag-expressions"></a>
     */
    @Parameter(property = "parallel.includeScenarioTags")
    private String includeScenarioTags;

    /**
     * Optional parallelization mode. By default, Cucable generates single scenarios (mode "scenarios").
     * When this property is set to "features", each generated feature file will be an exact copy of its source feature
     * so included scenarios are not split up and run in the same order.
     */
    @Parameter(property = "parallel.parallelizationMode", defaultValue = "scenarios")
    private String parallelizationMode;

    /**
     * Optional desired number of test runners that each run multiple features in sequence.
     */
    @Parameter(property = "parallel.desiredNumberOfRunners", defaultValue = "-1")
    private int desiredNumberOfRunners;

    /**
     * Optional desired number of features to run in sequence per test runner.
     */
    @Parameter(property = "parallel.desiredNumberOfFeaturesPerRunner", defaultValue = "0")
    private int desiredNumberOfFeaturesPerRunner;

    /**
     * Optional log level to control what information is logged in the console.
     * Allowed values: default, compact, minimal, off
     */
    @Parameter(property = "parallel.logLevel", defaultValue = "default")
    private String logLevel;

    /**
     * Optional custom parameters that are available inside the specified template file.
     * For example, the custom parameter &lt;test&gt;1&lt;/test&gt; will be available as [CUCABLE:CUSTOM:test].
     */
    @Parameter(property = "parallel.customPlaceholders")
    private Map<String, String> customPlaceholders;

    /**
     * Optional comma separated list of scenario names to run only scenarios whose names match at least one name
     * in the list. Number of runners created will be equal to the number of scenario names specified and each
     * runner will hold individual scenarios matching 1 scenario name. See also "--name" in Cucumber command-line
     * options ("java cucumber.api.cli.Main --help" or "mvn test -Dcucumber.options="--help"").
     */
    @Parameter(property = "parallel.scenarioNames")
    private String scenarioNames;

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



        // Initialize the log level
        logger.initialize(logLevel);

        // Initialize passed POM properties
        propertyManager.setSourceRunnerTemplateFile(sourceRunnerTemplateFile);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDirectory);
        propertyManager.setSourceFeatures(sourceFeatures);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDirectory);
        propertyManager.setNumberOfTestRuns(numberOfTestRuns);
        propertyManager.setIncludeScenarioTags(includeScenarioTags);
        propertyManager.setParallelizationMode(parallelizationMode);
        propertyManager.setCustomPlaceholders(customPlaceholders);
        propertyManager.setDesiredNumberOfRunners(desiredNumberOfRunners);
        propertyManager.setDesiredNumberOfFeaturesPerRunner(desiredNumberOfFeaturesPerRunner);
        propertyManager.setScenarioNames(scenarioNames);

        // Validate passed POM properties
        propertyManager.checkForMissingMandatoryProperties();
        propertyManager.checkForDisallowedPropertyCombinations();

        // Logging
        logPluginInformationHeader();
        propertyManager.logProperties();

        // Create the necessary directories if missing.
        fileManager.prepareGeneratedFeatureAndRunnerDirectories(
                propertyManager.getGeneratedRunnerDirectory(),
                propertyManager.getGeneratedFeatureDirectory()
        );
        // Conversion of scenarios into single scenarios and runners.
        featureFileConverter.generateParallelizableFeatures(propertyManager.getSourceFeatures());
    }

    /**
     * Log the plugin name and version.
     */
    private void logPluginInformationHeader() {
        CucableLogger.CucableLogLevel[] cucableLogLevels =
                new CucableLogger.CucableLogLevel[]{CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT};
        logger.logInfoSeparator(cucableLogLevels);
        logger.info(String.format(" Cucable Maven Plugin, version %s", getClass().getPackage().getImplementationVersion()), cucableLogLevels);
        logger.logInfoSeparator(cucableLogLevels);
    }
}



