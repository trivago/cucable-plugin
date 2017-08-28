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