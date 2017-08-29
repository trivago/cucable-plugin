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

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;

import java.util.List;

/**
 * Generate a scenario key from its position inside the feature file
 */
public class ScenarioKey {

    private final String key;

    public ScenarioKey(final Pickle scenario) {
        List<PickleLocation> scenarioLocations = scenario.getLocations();
        PickleLocation lastScenarioLocation = scenarioLocations.get(scenarioLocations.size() - 1);
        this.key = lastScenarioLocation.getLine() + "|" + lastScenarioLocation.getColumn();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ScenarioKey scenarioKey = (ScenarioKey) object;
        return key != null ? key.equals(scenarioKey.key) : scenarioKey.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}