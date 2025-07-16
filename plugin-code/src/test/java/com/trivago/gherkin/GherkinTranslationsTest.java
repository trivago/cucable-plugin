package com.trivago.gherkin;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GherkinTranslationsTest {
    private GherkinTranslations gherkinTranslations;

    @Before
    public void setup() {
        gherkinTranslations = new GherkinTranslations();
    }

    @Test
    public void debugScenarioKeywords() {
        System.out.println("English: " + gherkinTranslations.getScenarioKeyword("en"));
        System.out.println("German: " + gherkinTranslations.getScenarioKeyword("de"));
        System.out.println("French: " + gherkinTranslations.getScenarioKeyword("fr"));
        System.out.println("Spanish: " + gherkinTranslations.getScenarioKeyword("es"));
    }

    @Test
    public void getScenarioKeywordTesnt() {
        assertThat(gherkinTranslations.getScenarioKeyword("en"), is("Scenario"));
        assertThat(gherkinTranslations.getScenarioKeyword("de"), is("Szenario"));
        assertThat(gherkinTranslations.getScenarioKeyword("no"), is("Scenario"));
        assertThat(gherkinTranslations.getScenarioKeyword("ro"), is("Scenariu"));
        assertThat(gherkinTranslations.getScenarioKeyword("ru"), is("Сценарий"));
        assertThat(gherkinTranslations.getScenarioKeyword("fr"), is("Scénario"));
        assertThat(gherkinTranslations.getScenarioKeyword("gibberish"), is("Scenario"));
    }
}
