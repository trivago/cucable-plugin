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

package com.trivago.rta.vo;

import java.util.List;

/**
 * Holds a complete single scenario with all information.
 */
public final class SingleScenario {

    private final String featureDescription;
    private final String scenarioDescription;
    private String featureName;
    private List<String> featureTags;
    private String scenarioName;
    private List<String> scenarioTags;
    private List<Step> backgroundSteps;
    private List<Step> steps;

    public SingleScenario(
            final String featureName,
            final String featureDescription,
            final String scenarioName,
            final String scenarioDescription,
            final List<String> featureTags,
            final List<Step> backgroundSteps) {
        this.featureName = featureName;
        this.featureDescription = featureDescription;
        this.scenarioName = scenarioName;
        this.scenarioDescription = scenarioDescription;
        this.featureTags = featureTags;
        this.backgroundSteps = backgroundSteps;
    }

    public List<String> getFeatureTags() {
        return featureTags;
    }

    public String getFeatureName() {
        return featureName;
    }

    public String getFeatureDescription() {
        return featureDescription;
    }

    public List<String> getScenarioTags() {
        return scenarioTags;
    }

    public void setScenarioTags(final List<String> scenarioTags) {
        this.scenarioTags = scenarioTags;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public String getScenarioDescription() {
        return scenarioDescription;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(final List<Step> steps) {
        this.steps = steps;
    }

    public List<Step> getBackgroundSteps() {
        return backgroundSteps;
    }

    @Override
    public String toString() {
        return "SingleScenario{" +
                "featureName='" + featureName + '\'' +
                ", featureTags=" + featureTags +
                ", featureDescription='" + featureDescription + '\'' +
                ", scenarioName='" + scenarioName + '\'' +
                ", scenarioDescription='" + scenarioDescription + '\'' +
                ", scenarioTags=" + scenarioTags +
                ", backgroundSteps=" + backgroundSteps +
                ", steps=" + steps +
                '}';
    }
}
