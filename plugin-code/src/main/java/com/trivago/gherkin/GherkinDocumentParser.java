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
import com.trivago.exceptions.filesystem.FeatureFileParseException;
import com.trivago.logging.CucableLogger;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.SingleScenario;
import com.trivago.vo.Step;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Background;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.FeatureChild;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionException;
import io.cucumber.tagexpressions.TagExpressionParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.HashMap;

@Singleton
public class GherkinDocumentParser {

    private final GherkinToCucableConverter gherkinToCucableConverter;
    private final GherkinTranslations gherkinTranslations;
    private final PropertyManager propertyManager;
    private final CucableLogger cucableLogger;

    @Inject
    GherkinDocumentParser(
            final GherkinToCucableConverter gherkinToCucableConverter,
            final GherkinTranslations gherkinTranslations,
            final PropertyManager propertyManager,
            final CucableLogger logger
    ) {
        this.gherkinToCucableConverter = gherkinToCucableConverter;
        this.gherkinTranslations = gherkinTranslations;
        this.propertyManager = propertyManager;
        this.cucableLogger = logger;
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
            final List<Integer> scenarioLineNumbers
    ) throws CucablePluginException {
        String escapedFeatureContent = featureContent.replace("\\n", "\\\\n");
        List<SingleScenario> singleScenarioFeatures = new ArrayList<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(escapedFeatureContent.getBytes(StandardCharsets.UTF_8))) {
            GherkinParser gherkinParser = GherkinParser.builder().build();
            List<io.cucumber.messages.types.Pickle> pickles = new ArrayList<>();
            Feature feature = null;
            List<Envelope> envelopes = gherkinParser.parse(featureFilePath, inputStream).collect(Collectors.toList());
            
            // Check if parsing was successful
            boolean hasParseErrors = envelopes.stream()
                .anyMatch(envelope -> envelope.getParseError().isPresent());
            if (hasParseErrors) {
                throw new CucablePluginException("Failed to parse Gherkin feature file: " + featureFilePath);
            }
            
            for (Envelope envelope : envelopes) {
                if (envelope.getGherkinDocument().isPresent() && envelope.getGherkinDocument().get().getFeature().isPresent()) {
                    feature = envelope.getGherkinDocument().get().getFeature().get();
                }
                if (envelope.getPickle().isPresent()) {
                    pickles.add(envelope.getPickle().get());
                }
            }
            
            if (feature == null) {
                cucableLogger.warn("No parsable gherkin.");
                return Collections.emptyList();
            }
            
            String featureName = feature.getKeyword() + ": " + feature.getName();
            String featureLanguage = feature.getLanguage();
            String featureDescription = feature.getDescription();
            List<String> featureTags = gherkinToCucableConverter.convertGherkinTagsToCucableTags(feature.getTags());
            List<Step> backgroundSteps = getBackgroundSteps(feature);
            
            // Build a map from step ID to GherkinStep for keyword extraction
            Map<String, io.cucumber.messages.types.Step> stepIdMap = new HashMap<>();
            for (FeatureChild child : feature.getChildren()) {
                if (child.getBackground().isPresent()) {
                    for (io.cucumber.messages.types.Step step : child.getBackground().get().getSteps()) {
                        stepIdMap.put(step.getId(), step);
                    }
                }
                if (child.getScenario().isPresent()) {
                    for (io.cucumber.messages.types.Step step : child.getScenario().get().getSteps()) {
                        stepIdMap.put(step.getId(), step);
                    }
                }
            }
            
            for (io.cucumber.messages.types.Pickle pickle : pickles) {
                // REMOVED: background filtering logic. Always include all pickles.
                // REMOVED: check that skips pickles with empty or 'background' names.
                int lineNumber = 0; // Placeholder, as Pickle does not expose line directly
                String scenarioKeyword = gherkinTranslations.getScenarioKeyword(featureLanguage);
                String scenarioName = scenarioKeyword + ": " + pickle.getName();
                
                SingleScenario singleScenario = new SingleScenario(
                    featureName,
                    featureFilePath,
                    featureLanguage,
                    featureDescription,
                    scenarioName,
                    lineNumber,
                    "", // Pickle does not have a description
                    featureTags,
                    backgroundSteps
                );
                
                // Tags
                List<String> tags = pickle.getTags().stream()
                    .map(io.cucumber.messages.types.PickleTag::getName)
                    .collect(Collectors.toList());
                singleScenario.setScenarioTags(tags);
                
                // Collect background step IDs
                List<String> backgroundStepIds = new ArrayList<>();
                for (FeatureChild child : feature.getChildren()) {
                    if (child.getBackground().isPresent()) {
                        for (io.cucumber.messages.types.Step step : child.getBackground().get().getSteps()) {
                            backgroundStepIds.add(step.getId());
                        }
                    }
                }
                // Steps: only those not in backgroundStepIds
                List<Step> steps = pickle.getSteps().stream()
                    .filter(pickleStep -> pickleStep.getAstNodeIds().stream().noneMatch(backgroundStepIds::contains))
                    .map(pickleStep -> {
                        String stepText = pickleStep.getText();
                        String keyword = "";
                        // Find the original Gherkin step by AST node ID
                        for (String astId : pickleStep.getAstNodeIds()) {
                            io.cucumber.messages.types.Step gherkinStep = stepIdMap.get(astId);
                            if (gherkinStep != null) {
                                keyword = gherkinStep.getKeyword();
                                break;
                            }
                        }
                        // Handle DataTable and DocString from PickleStep argument (only one allowed)
                        com.trivago.vo.DataTable dataTable = null;
                        String docString = null;
                        if (pickleStep.getArgument().isPresent()) {
                            io.cucumber.messages.types.PickleStepArgument argument = pickleStep.getArgument().get();
                            if (argument.getDataTable().isPresent()) {
                                dataTable = gherkinToCucableConverter.convertPickleTableToCucableDataTable(
                                    argument.getDataTable().get());
                            }
                            if (argument.getDocString().isPresent()) {
                                docString = argument.getDocString().get().getContent();
                            }
                        }
                        return new Step(
                            (keyword + stepText).trim(),
                            dataTable,
                            docString
                        );
                    }).collect(Collectors.toList());
                singleScenario.setSteps(steps);
                
                if (scenarioShouldBeIncluded(singleScenario)) {
                    singleScenarioFeatures.add(singleScenario);
                }
            }
        } catch (CucablePluginException e) {
            throw e;
        } catch (Exception e) {
            throw new FeatureFileParseException(featureFilePath, e.getMessage());
        }
        
        return singleScenarioFeatures;
    }

    /**
     * Extracts background steps from a feature.
     */
    private List<Step> getBackgroundSteps(Feature feature) {
        for (FeatureChild child : feature.getChildren()) {
            if (child.getBackground().isPresent()) {
                Background background = child.getBackground().get();
                return gherkinToCucableConverter.convertGherkinStepsToCucableSteps(background.getSteps());
            }
        }
        return new ArrayList<>();
    }

    /**
     * Checks if a scenario should be included in the runner and feature generation based on the tag settings and
     * scenarioNames settings.
     *
     * @param singleScenario a single scenario object.
     * @return true if the combined tags match the given tag expression and the scenario name (if specified) matches.
     */
    private boolean scenarioShouldBeIncluded(final SingleScenario singleScenario) throws CucablePluginException {

        String includeScenarioTags = propertyManager.getIncludeScenarioTags();
        String language = singleScenario.getFeatureLanguage();
        String scenarioName = singleScenario.getScenarioName();
        boolean scenarioNameMatchExists = matchScenarioWithScenarioNames(language, scenarioName) >= 0;

        List<String> combinedScenarioTags = new ArrayList<>(singleScenario.getScenarioTags());
        combinedScenarioTags.addAll(singleScenario.getFeatureTags());
        combinedScenarioTags.addAll(singleScenario.getExampleTags());
        combinedScenarioTags = combinedScenarioTags.stream().distinct().collect(Collectors.toList());

        if (includeScenarioTags == null || includeScenarioTags.isEmpty()) {
            return scenarioNameMatchExists;
        }

        try {
            Expression tagExpression = TagExpressionParser.parse(includeScenarioTags);
            return tagExpression.evaluate(combinedScenarioTags) && scenarioNameMatchExists;
        } catch (TagExpressionException e) {
            throw new CucablePluginException(
                    "The tag expression '" + includeScenarioTags + "' is invalid: " + e.getMessage());
        }

    }

    /**
     * Checks if a scenarioName value matches with the scenario name.
     *
     * @param language      Feature file language ("en", "ro" etc).
     * @param stringToMatch the string that will be matched with the scenarioName value.
     * @return index of the scenarioName value in the scenarioNames list if a match exists.
     * -1 if no match exists.
     */
    public int matchScenarioWithScenarioNames(String language, String stringToMatch) {
        List<String> scenarioNames = propertyManager.getScenarioNames();
        String scenarioKeyword = gherkinTranslations.getScenarioKeyword(language);
        int matchIndex = -1;

        if (scenarioNames == null || scenarioNames.isEmpty()) {
            return 0;
        }

        for (String scenarioName : scenarioNames) {
            String regex = scenarioKeyword + ":.+" + scenarioName;
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(stringToMatch);
            if (matcher.find()) {
                matchIndex = scenarioNames.indexOf(scenarioName);
                break;
            }
        }

        return matchIndex;
    }
}