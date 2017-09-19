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
import gherkin.ast.TableCell;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GherkinDocumentParser {

    private final FileIO fileIO;
    private final GherkinToCucableConverter gherkinToCucableConverter;

    @Inject
    public GherkinDocumentParser(FileIO fileIO, GherkinToCucableConverter gherkinToCucableConverter) {
        this.fileIO = fileIO;
        this.gherkinToCucableConverter = gherkinToCucableConverter;
    }

    /**
     * Returns a {@link com.trivago.rta.vo.SingleScenario} list from a given feature file.
     *
     * @param featureFilePath the path to a feature file.
     * @return a {@link com.trivago.rta.vo.SingleScenario} list.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    public ArrayList<SingleScenario> getSingleScenariosFromFeatureFile(final Path featureFilePath) throws CucablePluginException {

        GherkinDocument gherkinDocument = getGherkinDocumentFromFeatureFile(featureFilePath);

//        List<Pickle> pickles = new Compiler().compile(gherkinDocument);

        Feature feature = gherkinDocument.getFeature();
        String featureName = feature.getName();
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
                System.out.println("============================ SCENARIO =================================");

                SingleScenario singleScenario = new SingleScenario(featureName, scenarioName, featureTags, backgroundSteps);
                Scenario scenario = (Scenario) scenarioDefinition;
                addScenarioInformationToSingleScenario(scenario, singleScenario);

                System.out.println(singleScenario);

                singleScenarioFeatures.add(singleScenario);
                continue;
            }

            if (scenarioDefinition instanceof ScenarioOutline) {
                System.out.println("============================ SCENARIO OUTLINE =========================");

                ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;

            }
        }

        System.out.println();

        return singleScenarioFeatures;
    }

    private void addScenarioOutlineInformationToSingleScenario(final ScenarioOutline scenarioOutline, final SingleScenario singleScenario) {
        // add scenario tags
        List<String> tags = gherkinToCucableConverter.convertGherkinTagsToCucableTags(scenarioOutline.getTags());
        singleScenario.setScenarioTags(tags);

        List<Examples> examples = scenarioOutline.getExamples();
        for (Examples example : examples) {
            System.out.println("Example name: " + example.getName());
            System.out.println("Example description: " + example.getDescription());
            System.out.println("Example keyword: " + example.getKeyword());
            System.out.println("Example tags: " + example.getTags());

            List<TableCell> cells = example.getTableHeader().getCells();
            for (TableCell cell : cells) {
                System.out.println("Table cell: " + cell.getValue());
            }

            System.out.println(example.getTableHeader().getCells());
            System.out.println(example.getTableBody());
        }

    }

    private void addScenarioInformationToSingleScenario(final Scenario scenario, final SingleScenario singleScenario) {
        List<String> tags = gherkinToCucableConverter.convertGherkinTagsToCucableTags(scenario.getTags());
        singleScenario.setScenarioTags(tags);

        List<Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(scenario.getSteps());
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
