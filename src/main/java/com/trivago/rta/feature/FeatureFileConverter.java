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

package com.trivago.rta.feature;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.FeatureFileParseException;
import com.trivago.rta.exceptions.FileCreationException;
import com.trivago.rta.exceptions.MissingFileException;
import com.trivago.rta.properties.PropertyManager;
import com.trivago.rta.runner.SingleScenarioRunner;
import gherkin.ParserException;
import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for converting feature files
 * into single scenario feature files and runners.
 */
@Singleton
public final class FeatureFileConverter {

    private final PropertyManager propertyManager;
    private final ScenarioParser scenarioParser;

    // Holds the current number of single features per feature key
    // (in a scenario outline, each example yields a single feature with the same key).
    private Map<String, Integer> singleFeatureCounters = new HashMap<>();

    @Inject
    public FeatureFileConverter(PropertyManager propertyManager, ScenarioParser scenarioParser) {
        this.propertyManager = propertyManager;
        this.scenarioParser = scenarioParser;
    }

    /**
     * Converts all scenarios in the given feature file to single
     * scenario feature files and their respective runners.
     *
     * @param featureFilePath feature file to process.
     * @throws CucablePluginException see {@link CucablePluginException}
     */
    public void convertToSingleScenariosAndRunners(final Path featureFilePath)
            throws CucablePluginException {

        GherkinDocument gherkinDocument;

        try {
            FileReader fileReader = new FileReader(featureFilePath.toFile());
            gherkinDocument = scenarioParser.parse(fileReader);
        } catch (FileNotFoundException e) {
            throw new MissingFileException(featureFilePath.toString());
        } catch (ParserException parserException) {
            throw new FeatureFileParseException(featureFilePath.toString());
        }

        // Store keywords ("Given", "When", "Then", "And")
        // for each step in the current scenario
        List<List<String>> scenarioKeywords = new ArrayList<>();
        List<String> stepKeywords;
        List<String> backgroundKeywords;

        Feature feature = gherkinDocument.getFeature();
        backgroundKeywords = new ArrayList<>();

        for (ScenarioDefinition scenario : feature.getChildren()) {
            if (scenario instanceof Background) {
                // Save background steps in order to add them to
                // all scenarios inside the same feature files
                scenario.getSteps().stream().map(step -> step.getKeyword().trim()).forEach(backgroundKeywords::add);
            } else {
                stepKeywords = new ArrayList<>();
                stepKeywords.addAll(backgroundKeywords);
                scenario.getSteps().stream().map(step -> step.getKeyword().trim()).forEach(stepKeywords::add);
                scenarioKeywords.add(stepKeywords);
            }
        }

        // Break feature file into scenarios
        List<Pickle> scenarios = new Compiler().compile(gherkinDocument);

        String fullFeatureFileName = featureFilePath.getFileName().toString();
        // remove extension
        int extensionPosition = fullFeatureFileName.lastIndexOf(".");
        String featureName =
                fullFeatureFileName.substring(0, extensionPosition);

        int scenarioKeywordIndex = 0;
        String previousScenarioKey = null;
        for (Pickle scenario : scenarios) {
            // Generate a scenario key from its position inside the feature file
            int lastLocationIndex = scenario.getLocations().size() - 1;
            PickleLocation scenarioPositionInFeatureFile =
                    scenario.getLocations().get(lastLocationIndex);
            String currentScenarioKey = scenarioPositionInFeatureFile.getLine()
                    + "|"
                    + scenarioPositionInFeatureFile.getColumn();

            // Set the old scenario key to the current one
            // (only done for the very first scenario)
            if (previousScenarioKey == null) {
                previousScenarioKey = currentScenarioKey;
                scenarioKeywordIndex = 0;
            }

            // Determine if the scenario changed from the one before by
            // comparing their keys (does not happen with scenario outlines)
            if (!currentScenarioKey.equals(previousScenarioKey)) {
                previousScenarioKey = currentScenarioKey;
                scenarioKeywordIndex++;
            }

            // Determine new feature file name
            // from the current scenario key's counter
            Integer featureCounter =
                    singleFeatureCounters.getOrDefault(featureName, 0);
            featureCounter++;

            // Add counter to filename
            String postfix = String.format("_%05d", featureCounter);
            // Append "_IT" to the filename so Failsafe considers
            // it an integration test automatically.
            String newFeatureName = featureName.concat(postfix).concat("_IT");

            String newFeatureFilePath = propertyManager.getGeneratedFeatureDirectory() + "/"
                    + newFeatureName.concat(".feature");
            singleFeatureCounters.put(featureName, featureCounter);

            // Save scenario information
            SingleScenarioFeature singleFeature =
                    new SingleScenarioFeature(
                            scenario.getTags(),
                            gherkinDocument.getFeature().getName(),
                            scenario.getName(),
                            scenario.getSteps(),
                            scenarioKeywords.get(scenarioKeywordIndex)
                    );

            // Save scenario information to new feature file
            try (PrintStream ps = new PrintStream(newFeatureFilePath)) {
                ps.println(singleFeature.getRenderedFeatureFileContent());
            } catch (FileNotFoundException e) {
                throw new FileCreationException(newFeatureName);
            }

            // Generate runner for the newly generated single scenario feature file
            SingleScenarioRunner singleScenarioRunner =
                    new SingleScenarioRunner(
                            propertyManager.getSourceRunnerTemplateFile(), newFeatureName);
            String newRunnerFilePath = propertyManager.getGeneratedRunnerDirectory() + "/"
                    + newFeatureName.concat(".java");
            try (PrintStream ps = new PrintStream(newRunnerFilePath)) {
                ps.println(singleScenarioRunner.getRenderedRunnerFileContent());
            } catch (IOException e) {
                throw new CucablePluginException(newRunnerFilePath);
            }
        }
    }
}
