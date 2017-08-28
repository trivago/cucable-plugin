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

package com.trivago.rta.vo;

import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a complete single scenario with all information like
 * tags, steps and keywords.
 */
public final class SingleScenarioFeature {
    // A list of tags for the current scenario.
    private List<String> tags;

    // The name of the feature this scenario belongs to.
    private String featureName;

    // The name of the scenario.
    private String name;

    // A list of steps inside this scenario (without the keywords).
    private List<String> steps;

    // A list of keywords (given, when, then, and) inside the scenario.
    private List<String> keywords;

    /**
     * Constructor for a single scenario.
     *
     * @param pickleTags        A list of tags in {@link PickleTag} format.
     * @param parentFeatureName The name of the feature of this scenario.
     * @param scenarioName      The name of this scenario.
     * @param pickleSteps       A list of steps in {@link PickleStep} format.
     * @param stepKeywords      A list of keywords (given, when, then, and).
     */
    public SingleScenarioFeature(
            final List<PickleTag> pickleTags,
            final String parentFeatureName,
            final String scenarioName,
            final List<PickleStep> pickleSteps,
            final List<String> stepKeywords
    ) {
        this.featureName = parentFeatureName;
        this.name = scenarioName;

        tags = new ArrayList<>();
        for (PickleTag pickleTag : pickleTags) {
            tags.add(pickleTag.getName());
        }

        steps = new ArrayList<>();
        for (PickleStep pickleStep : pickleSteps) {
            steps.add(pickleStep.getText());
        }

        this.keywords = stepKeywords;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getName() {
        return name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    @Override
    public String toString() {
        return "SingleScenarioFeature{"
                + "tags=" + tags
                + ", featureName='" + featureName + '\''
                + ", name='" + name + '\''
                + ", steps=" + steps
                + ", keywords=" + keywords
                + '}';
    }
}
