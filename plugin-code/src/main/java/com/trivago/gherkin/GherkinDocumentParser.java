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

package com.trivago.gherkin;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.DataTable;
import com.trivago.vo.SingleScenario;
import com.trivago.vo.Step;
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
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionException;
import io.cucumber.tagexpressions.TagExpressionParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class GherkinDocumentParser {

    private static final Pattern SCENARIO_OUTLINE_PLACEHOLDER_PATTERN = Pattern.compile("<.+?>");

    private final TagExpressionParser tagExpressionParser = new TagExpressionParser();
    private final GherkinToCucableConverter gherkinToCucableConverter;
    private final GherkinTranslations gherkinTranslations;
    private final PropertyManager propertyManager;

    @Inject
    GherkinDocumentParser(
            final GherkinToCucableConverter gherkinToCucableConverter,
            final GherkinTranslations gherkinTranslations,
            final PropertyManager propertyManager
    ) {
        this.gherkinToCucableConverter = gherkinToCucableConverter;
        this.gherkinTranslations = gherkinTranslations;
        this.propertyManager = propertyManager;
    }

    /**
     * Returns a {@link SingleScenario} list from a given feature file.
     *
     * @param featureContent  A feature string.
     * @param featureFilePath The path to the source feature file.
     * @return A {@link SingleScenario} list.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    public List<SingleScenario> getSingleScenariosFromFeature(
            final String featureContent,
            final String featureFilePath,
            final List<Integer> scenarioLineNumbers) throws CucablePluginException {

        String escapedFeatureContent = featureContent.replace("\\n", "\\\\n");
        GherkinDocument gherkinDocument = getGherkinDocumentFromFeatureFileContent(escapedFeatureContent);

        Feature feature = gherkinDocument.getFeature();
        String featureName = feature.getKeyword() + ": " + feature.getName();
        String featureLanguage = feature.getLanguage();
        String featureDescription = feature.getDescription();
        List<String> featureTags = gherkinToCucableConverter.convertGherkinTagsToCucableTags(feature.getTags());

        ArrayList<SingleScenario> singleScenarioFeatures = new ArrayList<>();
        List<Step> backgroundSteps = new ArrayList<>();

        List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();
        for (ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            String scenarioName = scenarioDefinition.getKeyword() + ": " + scenarioDefinition.getName();
            String scenarioDescription = scenarioDefinition.getDescription();

            if (scenarioDefinition instanceof Background) {
                // Save background steps in order to add them to every scenario inside the same feature
                Background background = (Background) scenarioDefinition;
                backgroundSteps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(background.getSteps());
                continue;
            }

            if (scenarioDefinition instanceof Scenario) {
                Scenario scenario = (Scenario) scenarioDefinition;
                if (scenarioLineNumbers == null
                        || scenarioLineNumbers.isEmpty()
                        || scenarioLineNumbers.contains(scenario.getLocation().getLine())) {
                    SingleScenario singleScenario =
                            new SingleScenario(
                                    featureName,
                                    featureFilePath,
                                    featureLanguage,
                                    featureDescription,
                                    scenarioName,
                                    scenarioDescription,
                                    featureTags,
                                    backgroundSteps
                            );
                    addGherkinScenarioInformationToSingleScenario(scenario, singleScenario);
                    if (scenarioShouldBeIncluded(
                            singleScenario.getScenarioTags(),
                            singleScenario.getFeatureTags()
                    )) {
                        singleScenarioFeatures.add(singleScenario);
                    }
                }
                continue;
            }

            if (scenarioDefinition instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;

                if (scenarioLineNumbers == null
                        || scenarioLineNumbers.isEmpty()
                        || scenarioLineNumbers.contains(scenarioOutline.getLocation().getLine())) {
                    List<SingleScenario> outlineScenarios =
                            getSingleScenariosFromOutline(
                                    scenarioOutline,
                                    featureName,
                                    featureFilePath,
                                    featureLanguage,
                                    featureDescription,
                                    featureTags,
                                    backgroundSteps
                            );
                    for (SingleScenario singleScenario : outlineScenarios) {
                        if (scenarioShouldBeIncluded(
                                singleScenario.getScenarioTags(),
                                singleScenario.getFeatureTags(),
                                singleScenario.getExampleTags()
                        )) {
                            singleScenarioFeatures.add(singleScenario);
                        }
                    }
                }
            }
        }
        return singleScenarioFeatures;
    }

    /**
     * Returns a list of Cucable single scenarios from a Gherkin scenario outline.
     *
     * @param scenarioOutline A Gherkin {@link ScenarioOutline}.
     * @param featureName     The name of the feature this scenario outline belongs to.
     * @param featureFilePath The source path of the feature file.
     * @param featureLanguage The feature language this scenario outline belongs to.
     * @param featureTags     The feature tags of the parent feature.
     * @param backgroundSteps Return a Cucable {@link SingleScenario} list.
     * @throws CucablePluginException Thrown when the scenario outline does not contain an example table.
     */
    private List<SingleScenario> getSingleScenariosFromOutline(
            final ScenarioOutline scenarioOutline,
            final String featureName,
            final String featureFilePath,
            final String featureLanguage,
            final String featureDescription,
            final List<String> featureTags,
            final List<Step> backgroundSteps
    ) throws CucablePluginException {

        // Retrieve the translation of "Scenario" in the target language and add it to the scenario
        String translatedScenarioKeyword = gherkinTranslations.getScenarioKeyword(featureLanguage);
        String scenarioName = translatedScenarioKeyword + ": " + scenarioOutline.getName();

        String scenarioDescription = scenarioOutline.getDescription();
        List<String> scenarioTags =
                gherkinToCucableConverter.convertGherkinTagsToCucableTags(scenarioOutline.getTags());

        List<SingleScenario> outlineScenarios = new ArrayList<>();
        List<Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(scenarioOutline.getSteps());

        if (scenarioOutline.getExamples().isEmpty()) {
            throw new CucablePluginException("Scenario outline without examples table!");
        }

        for (Examples exampleTable : scenarioOutline.getExamples()) {
            Map<String, List<String>> exampleMap =
                    gherkinToCucableConverter.convertGherkinExampleTableToCucableExampleMap(exampleTable);

            String firstColumnHeader = (String) exampleMap.keySet().toArray()[0];
            int rowCount = exampleMap.get(firstColumnHeader).size();

            // For each example row, create a new single scenario
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                SingleScenario singleScenario =
                        new SingleScenario(
                                featureName,
                                featureFilePath,
                                featureLanguage,
                                featureDescription,
                                replacePlaceholderInString(scenarioName, exampleMap, rowIndex),
                                scenarioDescription,
                                featureTags,
                                backgroundSteps
                        );

                List<Step> substitutedSteps = substituteStepExamplePlaceholders(steps, exampleMap, rowIndex);
                singleScenario.setSteps(substitutedSteps);
                singleScenario.setScenarioTags(scenarioTags);
                singleScenario.setExampleTags(
                        gherkinToCucableConverter.convertGherkinTagsToCucableTags(exampleTable.getTags())
                );
                outlineScenarios.add(singleScenario);
            }
        }

        return outlineScenarios;
    }

    /**
     * Replaces the example value placeholders in steps by the actual example table values.
     *
     * @param steps      The Cucable {@link Step} list.
     * @param exampleMap The generated example map from an example table.
     * @param rowIndex   The row index of the example table to consider.
     * @return A {@link Step} list with substituted names.
     */
    private List<Step> substituteStepExamplePlaceholders(
            final List<Step> steps, final Map<String, List<String>> exampleMap, final int rowIndex) {

        List<Step> substitutedSteps = new ArrayList<>();
        for (Step step : steps) {
            String stepName = step.getName();
            // substitute values in the step
            DataTable dataTable = step.getDataTable();

            String substitutedStepName = replacePlaceholderInString(stepName, exampleMap, rowIndex);
            DataTable substitutedDataTable = replaceDataTableExamplePlaceholder(dataTable, exampleMap, rowIndex);

            substitutedSteps.add(new Step(substitutedStepName, substitutedDataTable, step.getDocString()));
        }

        return substitutedSteps;
    }

    /**
     * Replaces the example value placeholders in step data tables by the actual example table values.
     *
     * @param dataTable  The source {@link DataTable}.
     * @param exampleMap The generated example map from an example table.
     * @param rowIndex   The row index of the example table to consider.
     * @return The resulting {@link DataTable}.
     */
    private DataTable replaceDataTableExamplePlaceholder(
            final DataTable dataTable,
            final Map<String, List<String>> exampleMap,
            final int rowIndex
    ) {
        if (dataTable == null) {
            return null;
        }

        List<List<String>> dataTableRows = dataTable.getRows();
        DataTable replacedDataTable = new DataTable();
        for (List<String> dataTableRow : dataTableRows) {
            List<String> replacedDataTableRow = new ArrayList<>();
            for (String dataTableCell : dataTableRow) {
                replacedDataTableRow.add(replacePlaceholderInString(dataTableCell, exampleMap, rowIndex));
            }
            replacedDataTable.addRow(replacedDataTableRow);
        }

        return replacedDataTable;
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
     * @param featureContent a feature string.
     * @return a {@link GherkinDocument}.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    private GherkinDocument getGherkinDocumentFromFeatureFileContent(final String featureContent)
            throws CucablePluginException {

        Parser<GherkinDocument> gherkinDocumentParser = new Parser<>(new AstBuilder());
        GherkinDocument gherkinDocument;

        try {
            gherkinDocument = gherkinDocumentParser.parse(featureContent);
        } catch (ParserException parserException) {
            throw new CucablePluginException("Could not parse feature: " + parserException.getMessage());
        }

        if (gherkinDocument == null || gherkinDocument.getFeature() == null) {
            throw new CucablePluginException("Could not parse features from gherkin document!");
        }

        return gherkinDocument;
    }

    /**
     * Checks if a scenario should be included in the runner and feature generation based on the tag settings.
     *
     * @param sourceTagsList any number of source tag lists to be considered
     * @return true if an include tag  and no exclude tags are included in the source tag list.
     */
    @SafeVarargs
    private final boolean scenarioShouldBeIncluded(final List<String>... sourceTagsList) throws CucablePluginException {

        String includeScenarioTags = propertyManager.getIncludeScenarioTags();

        List<String> combinedScenarioTags = new ArrayList<>();
        for (List<String> sourceTags : sourceTagsList) {
            combinedScenarioTags.addAll(sourceTags);
        }

        if (includeScenarioTags == null || includeScenarioTags.isEmpty()) {
            return true;
        }

        Expression tagExpression;
        try {
            tagExpression = tagExpressionParser.parse(includeScenarioTags);
        } catch (TagExpressionException e) {
            throw new CucablePluginException("The tag expression '" + includeScenarioTags + "' is invalid: " + e.getMessage());
        }
        return tagExpression.evaluate(combinedScenarioTags);
    }

    /**
     * Replaces the example value placeholders in a String by the actual example table values.
     *
     * @param sourceString The source string.
     * @param exampleMap   The generated example map from an example table.
     * @param rowIndex     The row index of the example table to consider.
     * @return a {@link String} with placeholders substituted for actual values from the example table.
     */
    private String replacePlaceholderInString(
            final String sourceString,
            final Map<String, List<String>> exampleMap,
            final int rowIndex) {

        String result = sourceString;
        Matcher m = SCENARIO_OUTLINE_PLACEHOLDER_PATTERN.matcher(sourceString);
        while (m.find()) {
            String currentPlaceholder = m.group(0);
            List<String> placeholderColumn = exampleMap.get(currentPlaceholder);
            if (placeholderColumn != null) {
                result = result.replace(currentPlaceholder, placeholderColumn.get(rowIndex));
            }
        }
        return result;
    }
}