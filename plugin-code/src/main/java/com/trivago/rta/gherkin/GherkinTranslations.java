package com.trivago.rta.gherkin;

import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class GherkinTranslations {

    private static final String SCENARIO = "Scenario";
    private final GherkinDialectProvider gherkinDialectProvider;

    @Inject
    GherkinTranslations() {
        gherkinDialectProvider = new GherkinDialectProvider();
    }

    String getScenarioKeyword(final String language) {
        GherkinDialect dialect;
        try {
            dialect = gherkinDialectProvider.getDialect(language, null);
        } catch (Exception e) {
            return SCENARIO;
        }
        return dialect.getScenarioKeywords().get(0);
    }
}
