package com.trivago.rta.vo;

import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ScenarioKeyTest {

    @Test
    public void scenarioKeysFromSameScenarioLocationsShouldBeEqual() {
        List<PickleLocation> locations = new ArrayList<>();
        locations.add(new PickleLocation(1, 1));
        Pickle scenario1 = new Pickle("scenario1", "de", new ArrayList<>(), new ArrayList<>(), locations);
        ScenarioKey scenarioKey1 = new ScenarioKey(scenario1);
        ScenarioKey scenarioKey2 = new ScenarioKey(scenario1);

        assertThat(scenarioKey1, is(scenarioKey2));
    }

    @Test
    public void scenarioKeysFromDifferentScenariosWithSameLocationsShouldBeEqual() {
        List<PickleLocation> locations = new ArrayList<>();
        locations.add(new PickleLocation(1, 1));
        Pickle scenario1 = new Pickle("scenario1", "de", new ArrayList<>(), new ArrayList<>(), locations);
        ScenarioKey scenarioKey1 = new ScenarioKey(scenario1);

        Pickle scenario2 = new Pickle("scenario1", "de", new ArrayList<>(), new ArrayList<>(), locations);
        ScenarioKey scenarioKey2 = new ScenarioKey(scenario2);

        assertThat(scenarioKey1, is(scenarioKey2));
    }

    @Test
    public void scenarioKeysFromDifferentScenariosWithDifferentLocationsShouldBeDifferent() {
        List<PickleLocation> locations1 = new ArrayList<>();
        locations1.add(new PickleLocation(1, 1));
        Pickle scenario1 = new Pickle("scenario1", "de", new ArrayList<>(), new ArrayList<>(), locations1);
        ScenarioKey scenarioKey1 = new ScenarioKey(scenario1);

        List<PickleLocation> locations2 = new ArrayList<>();
        locations2.add(new PickleLocation(2, 2));
        Pickle scenario2 = new Pickle("scenario1", "de", new ArrayList<>(), new ArrayList<>(), locations2);
        ScenarioKey scenarioKey2 = new ScenarioKey(scenario2);

        assertThat(scenarioKey1, is(not(scenarioKey2)));
    }
}
