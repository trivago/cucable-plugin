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

package com.trivago.rta.gherkin;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.vo.DataTable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class GherkinDocumentParser {

    private final GherkinToCucableConverter gherkinToCucableConverter;
    private final GherkinTranslations gherkinTranslations;

    @Inject
    public GherkinDocumentParser(
            final GherkinToCucableConverter gherkinToCucableConverter,
            final GherkinTranslations gherkinTranslations
    ) {
        this.gherkinToCucableConverter = gherkinToCucableConverter;
        this.gherkinTranslations = gherkinTranslations;
    }

    /**
     * Returns a {@link com.trivago.rta.vo.SingleScenario} list from a given feature file.
     *
     * @param featureContent      A feature string.
     * @param featureFilePath     The path to the source feature file.
     * @param scenarioLineNumbers An optional line number of a scenario inside a feature file.
     * @param includeScenarioTags Optional scenario tags to include into scenario generation.
     * @param excludeScenarioTags Optional scenario tags to exclude from scenario generation.
     * @return A {@link com.trivago.rta.vo.SingleScenario} list.
     * @throws CucablePluginException see {@link CucablePluginException}.
     */
    public List<SingleScenario> getSingleScenariosFromFeature(
            final String featureContent,
            final String featureFilePath,
            final List<Integer> scenarioLineNumbers,
            final List<String> includeScenarioTags,
            final List<String> excludeScenarioTags) throws CucablePluginException {

        GherkinDocument gherkinDocument = getGherkinDocumentFromFeatureFileContent(featureContent);
        Feature feature = gherkinDocument.getFeature();
        String featureName = feature.getKeyword() + ": " + feature.getName();
        String featureLanguage = feature.getLanguage();
        String featureDescription = feature.getDescription();
        List<String> featureTags =
                gherkinToCucableConverter.convertGherkinTagsToCucableTags(feature.getTags());

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
                            singleScenario.getFeatureTags(),
                            includeScenarioTags,
                            excludeScenarioTags
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
                                    backgroundSteps,
                                    includeScenarioTags,
                                    excludeScenarioTags
                            );
                    singleScenarioFeatures.addAll(outlineScenarios);
                }
            }
        }
        return singleScenarioFeatures;
    }

    /**
     * Returns a list of Cucable single scenarios from a Gherkin scenario outline.
     *
     * @param scenarioOutline     A Gherkin {@link ScenarioOutline}.
     * @param featureName         The name of the feature this scenario outline belongs to.
     * @param featureFilePath     The source path of the feature file.
     * @param featureLanguage     The feature language this scenario outline belongs to.
     * @param featureTags         The feature tags of the parent feature.
     * @param backgroundSteps     Return a Cucable {@link SingleScenario} list.
     * @param includeScenarioTags Optional scenario tags to include in scenario generation.
     * @throws CucablePluginException Thrown when the scenario outline does not contain an example table.
     */
    private List<SingleScenario> getSingleScenariosFromOutline(
            final ScenarioOutline scenarioOutline,
            final String featureName,
            final String featureFilePath,
            final String featureLanguage,
            final String featureDescription,
            final List<String> featureTags,
            final List<Step> backgroundSteps,
            final List<String> includeScenarioTags,
            final List<String> excludeScenarioTags
    ) throws CucablePluginException {

        // Retrieve the translation of "Scenario" in the target language and add it to the scenario
        String translatedScenarioKeyword = gherkinTranslations.getScenarioKeyword(featureLanguage);
        String scenarioName = translatedScenarioKeyword + ": " + scenarioOutline.getName();

        String scenarioDescription = scenarioOutline.getDescription();
        List<String> scenarioTags =
                gherkinToCucableConverter.convertGherkinTagsToCucableTags(scenarioOutline.getTags());

        if (!scenarioShouldBeIncluded(featureTags, scenarioTags, includeScenarioTags, excludeScenarioTags)) {
            return Collections.emptyList();
        }

        List<SingleScenario> outlineScenarios = new ArrayList<>();

        List<Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(scenarioOutline.getSteps());

        if (scenarioOutline.getExamples().isEmpty()) {
            throw new CucablePluginException("Scenario outline without examples table!");
        }

        Examples exampleTable = scenarioOutline.getExamples().get(0);
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
                            substituteScenarioNameExamplePlaceholders(scenarioName, exampleMap, rowIndex),
                            scenarioDescription,
                            featureTags,
                            backgroundSteps
                    );

            List<Step> substitutedSteps = substituteStepExamplePlaceholders(steps, exampleMap, rowIndex);
            singleScenario.setSteps(substitutedSteps);
            singleScenario.setScenarioTags(scenarioTags);
            outlineScenarios.add(singleScenario);
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
            for (String columnName : exampleMap.keySet()) {
                String columnValue = exampleMap.get(columnName).get(rowIndex);
                stepName = stepName.replace(columnName, columnValue);
                dataTable = replaceDataTableExamplePlaceholder(dataTable, columnName, columnValue);
            }
            substitutedSteps.add(new Step(stepName, dataTable, step.getDocString()));
        }

        return substitutedSteps;
    }

    /**
     * Replaces the example value placeholders in step data tables by the actual example table values.
     *
     * @param dataTable   The source {@link DataTable}.
     * @param columnName  The current placeholder to replace with a value.
     * @param columnValue The current value to replace.
     * @return The resulting {@link DataTable}.
     */
    private DataTable replaceDataTableExamplePlaceholder(
            final DataTable dataTable,
            final String columnName,
            final String columnValue
    ) {
        if (dataTable == null) {
            return null;
        }

        List<List<String>> dataTableRows = dataTable.getRows();
        DataTable replacedDataTable = new DataTable();
        for (List<String> dataTableRow : dataTableRows) {
            List<String> replacedDataTableRow = new ArrayList<>();
            for (String dataTableCell : dataTableRow) {
                replacedDataTableRow.add(dataTableCell.replace(columnName, columnValue));
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
            throw new CucablePluginException("Could not parse feature!");
        }

        if (gherkinDocument == null || gherkinDocument.getFeature() == null) {
            throw new CucablePluginException("Could not parse feature!");
        }

        return gherkinDocument;
    }

    /**
     * Checks if a scenario should be included in the runner and feature generation based on the tag settings.
     *
     * @param featureTags         the source feature tag list.
     * @param scenarioTags        the source scenario tag list.
     * @param includeScenarioTags the include tags list.
     * @param excludeScenarioTags the exclude tags list.
     * @return true if an include tag  and no exclude tags are included in the source tag list.
     */
    private boolean scenarioShouldBeIncluded(
            final List<String> featureTags,
            final List<String> scenarioTags,
            final List<String> includeScenarioTags,
            final List<String> excludeScenarioTags
    ) {

        // Combine scenario and feature tags to check against both.
        List<String> combinedSourceTags = new ArrayList<>();
        combinedSourceTags.addAll(featureTags);
        combinedSourceTags.addAll(scenarioTags);

        // If there are no scenario tags but include scenario tags, this scenario cannot be included.
        // If there are no scenario tags and no include scenario tags, this scenario can be directly included.
        if (combinedSourceTags.isEmpty()) {
            return includeScenarioTags == null || includeScenarioTags.isEmpty();
        } else {
            boolean result = false;
            for (String sourceTag : combinedSourceTags) {
                if (includeScenarioTags != null && !includeScenarioTags.isEmpty()) {
                    // If there are include scenario tags, check if any scenario tag matches any include tag...
                    for (String includeScenarioTag : includeScenarioTags) {
                        if (sourceTag.equalsIgnoreCase(includeScenarioTag)) {
                            result = true;
                            break;
                        }
                    }
                } else {
                    // ...else include all.
                    result = true;
                }

                // If there are exclude scenario tags, check if any scenario tag matches any exclude tag.
                if (excludeScenarioTags != null && !excludeScenarioTags.isEmpty()) {
                    for (String excludeScenarioTag : excludeScenarioTags) {
                        if (sourceTag.equalsIgnoreCase(excludeScenarioTag)) {
                            return false;
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * Replaces the example value placeholders in ScenarioOutline name by the actual example table values.
     *
     * @param scenarioOutlineName The ScenarioOutline generic name.
     * @param exampleMap          The generated example map from an example table.
     * @param rowIndex            The row index of the example table to consider.
     * @return a {@link String} name with placeholders substituted for actual values from example table.
     */

    private String substituteScenarioNameExamplePlaceholders(
            final String scenarioOutlineName,
            final Map<String, List<String>> exampleMap,
            final int rowIndex) {
        String result = scenarioOutlineName;
        String placeholderPattern = "<.+?>";
        Pattern p = Pattern.compile(placeholderPattern);
        Matcher m = p.matcher(scenarioOutlineName);

        while (m.find()) {
            String currentPlaceholder = m.group(0);
            result = result.replace(currentPlaceholder, exampleMap.get(currentPlaceholder).get(rowIndex));
        }

        return result;
    }
}

