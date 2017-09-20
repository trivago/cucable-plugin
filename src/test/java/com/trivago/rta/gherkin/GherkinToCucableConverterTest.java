package com.trivago.rta.gherkin;

import gherkin.ast.Location;
import gherkin.ast.Step;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GherkinToCucableConverterTest {
    private GherkinToCucableConverter gherkinToCucableConverter;

    @Before
    public void setup() {
        gherkinToCucableConverter = new GherkinToCucableConverter();
    }

    @Test
    public void convertGherkinStepsToCucableStepsTest() {
        List<Step> gherkinSteps = Arrays.asList(
                new Step(new Location(1, 1),
                        "Given ", "this is a test step", null),
                new Step(new Location(2, 1),
                        "Then ", "I get a test result", null)
        );

        List<com.trivago.rta.vo.Step> steps = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(gherkinSteps);
        assertThat(steps.size(), is(gherkinSteps.size()));
        com.trivago.rta.vo.Step firstStep = steps.get(0);
        assertThat(firstStep.getName(), is("Given this is a test step"));
        com.trivago.rta.vo.Step secondStep = steps.get(1);
        assertThat(secondStep.getName(), is("Then I get test results"));
    }
}
