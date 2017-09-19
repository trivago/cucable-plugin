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

package com.trivago.rta.gherkin;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FileIO;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.vo.SingleScenario;
import com.trivago.rta.vo.Step;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.Background;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class GherkinDocumentParser {

    private final FileIO fileIO;
    private final GherkinToCucableConverter gherkinToCucableConverter;
    private final CucableLogger logger;

    @Inject
    public GherkinDocumentParser(
            FileIO fileIO, GherkinToCucableConverter gherkinToCucableConverter, CucableLogger logger) {

        this.fileIO = fileIO;
        this.gherkinToCucableConverter = gherkinToCucableConverter;
        this.logger = logger;
    }

    /**
     * Returns a {@link com.trivago.rta.vo.SingleScenario} list from a given feature file.
     *
     * @param featureFilePath the path to a feature file.
     * @return a {@link com.trivago.rta.vo.SingleScenario} list.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    public List<SingleScenario> getSingleScenariosFromFeatureFile(final Path featureFilePath) throws CucablePluginException {

        GherkinDocument gherkinDocument = getGherkinDocumentFromFeatureFile(featureFilePath);

        Feature feature = gherkinDocument.getFeature();
        String featureName = feature.getName();

        logger.info("Feature: " + featureName);

        List<String> featureTags =
                gherkinToCucableConverter.convertGherkinTagsToCucableTags(feature.getTags());

        List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();

        ArrayList<SingleScenario> singleScenarioFeatures = new ArrayList<>();
        List<Step> backgroundSteps = new ArrayList<>();

        for (ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            String scenarioName = scenarioDefinition.getName();

            if (scenarioDefinition instanceof Background) {
                // Save background steps in order to add them to every scenario inside the same feature
                Background background = (Background) scenarioDefinition;
                backgroundSteps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(background.getSteps());
                continue;
            }

            if (scenarioDefinition instanceof Scenario) {
                SingleScenario singleScenario = new SingleScenario(featureName, scenarioName, featureTags, backgroundSteps);
                Scenario scenario = (Scenario) scenarioDefinition;
                addGherkinScenarioInformationToSingleScenario(scenario, singleScenario);
                singleScenarioFeatures.add(singleScenario);
                continue;
            }

            if (scenarioDefinition instanceof ScenarioOutline) {
                System.out.println("============================ SCENARIO OUTLINE =========================");

                ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;
                List<SingleScenario> outlineScenarios =
                        getSingleScenariosFromOutline(scenarioOutline, featureName, featureTags, backgroundSteps);
                singleScenarioFeatures.addAll(outlineScenarios);
            }
        }

        System.out.println("---");
        return singleScenarioFeatures;
    }

    /**
     * Returns a list of Cucable single scenarios from a Gherkin scenario outline.
     *
     * @param scenarioOutline a Gherkin {@link ScenarioOutline}.
     * @param featureName The name of the feature this scenario outline belongs to.
     * @param featureTags The feature tags of the parent feature.
     * @param backgroundSteps return a Cucable {@link SingleScenario} list.
     * @throws CucablePluginException thrown when the scenario outline does not contain an example table.
     */
    private List<SingleScenario> getSingleScenariosFromOutline(final ScenarioOutline scenarioOutline, final String featureName, final List<String> featureTags, final List<Step> backgroundSteps)
            throws CucablePluginException {

        String scenarioName = scenarioOutline.getName();
        List<String> scenarioTags =
                gherkinToCucableConverter.convertGherkinTagsToCucableTags(scenarioOutline.getTags());

        List<SingleScenario> outlineScenarios = new ArrayList<>();

        List<Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(scenarioOutline.getSteps());

        if (scenarioOutline.getExamples().isEmpty()) {
            throw new CucablePluginException("Scenario outline without examples table!");
        }

        Examples exampleTable = scenarioOutline.getExamples().get(0);
        Map<String, List<String>> exampleMap =
                gherkinToCucableConverter.convertGherkinExampleTableToCucableExampleMap(exampleTable);

        // for each example row, create a new single scenario
        for (int i = 0; i < exampleMap.values().size(); i++) {
            SingleScenario singleScenario =
                    new SingleScenario(featureName, scenarioName, featureTags, backgroundSteps);
            singleScenario.setScenarioTags(scenarioTags);
            outlineScenarios.add(singleScenario);
        }

        return outlineScenarios;
    }

    /**
     * Adds tags and steps from a Gherkin scenario to an existing single scenario.
     *
     * @param gherkinScenario a Gherkin {@link Scenario}.
     * @param singleScenario  an existing Cucable {@link SingleScenario}.
     */
    private void addGherkinScenarioInformationToSingleScenario(
            final Scenario gherkinScenario, final SingleScenario singleScenario) {

        List<String> tags = gherkinToCucableConverter.convertGherkinTagsToCucableTags(gherkinScenario.getTags());
        singleScenario.setScenarioTags(tags);

        List<Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(gherkinScenario.getSteps());
        singleScenario.setSteps(steps);
    }

    /**
     * Get a {@link GherkinDocument} from a feature file for further processing.
     *
     * @param featureFilePath the path to a feature file.
     * @return a {@link GherkinDocument}.
     * @throws MissingFileException      see {@link MissingFileException}.
     * @throws FeatureFileParseException see {@link FeatureFileParseException}.
     */
    private GherkinDocument getGherkinDocumentFromFeatureFile(final Path featureFilePath)
            throws MissingFileException, FeatureFileParseException {

        Parser<GherkinDocument> gherkinDocumentParser = new Parser<>(new AstBuilder());
        GherkinDocument gherkinDocument;

        try {
            String content = fileIO.readContentFromFile(featureFilePath.toString());
            gherkinDocument = gherkinDocumentParser.parse(content);
        } catch (ParserException parserException) {
            throw new FeatureFileParseException(featureFilePath.toString());
        }

        if (gherkinDocument == null) {
            throw new FeatureFileParseException(featureFilePath.toString());
        }

        return gherkinDocument;
    }
}
