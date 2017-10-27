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
    private static final String DATA_TABLE_SEPARATOR = "|";

    private String featureName;
    private List<String> featureTags;
    private String scenarioName;
    private List<String> scenarioTags;
    private List<Step> backgroundSteps;
    private List<Step> steps;

    public SingleScenario(
            final String featureName,
            final String scenarioName,
            final List<String> featureTags,
            final List<Step> backgroundSteps) {
        this.featureName = featureName;
        this.scenarioName = scenarioName;
        this.featureTags = featureTags;
        this.backgroundSteps = backgroundSteps;
    }

    public List<String> getFeatureTags() {
        return featureTags;
    }

    public String getFeatureName() {
        return featureName;
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
                ", scenarioName='" + scenarioName + '\'' +
                ", scenarioTags=" + scenarioTags +
                ", backgroundSteps=" + backgroundSteps +
                ", steps=" + steps +
                '}';
    }
}
