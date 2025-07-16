package com.trivago.gherkin;

import io.cucumber.gherkin.GherkinDialectProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
class GherkinTranslations {

    private static final String SCENARIO = "Scenario";

    private final GherkinDialectProvider gherkinDialectProvider;

    @Inject
    GherkinTranslations() {
        gherkinDialectProvider = new GherkinDialectProvider();
    }

    String getScenarioKeyword(final String language) {
        try {
            return gherkinDialectProvider.getDialect(language)
                    .map(dialect -> {
                        List<String> scenarioKeywords = dialect.getScenarioKeywords();
                        // Prefer 'Scenario' (or translation) over 'Example'
                        for (String keyword : scenarioKeywords) {
                            if (keyword.trim().equalsIgnoreCase("Scenario") || keyword.trim().equalsIgnoreCase("Szenario") || keyword.trim().equalsIgnoreCase("Scénario") || keyword.trim().equalsIgnoreCase("Scenariu") || keyword.trim().equalsIgnoreCase("Сценарий")) {
                                return keyword.trim();
                            }
                        }
                        if (!scenarioKeywords.isEmpty()) {
                            return scenarioKeywords.get(0).trim();
                        }
                        List<String> exampleKeywords = dialect.getExamplesKeywords();
                        if (!exampleKeywords.isEmpty()) {
                            return exampleKeywords.get(0).trim();
                        }
                        return SCENARIO;
                    })
                    .orElse(SCENARIO);
        } catch (Exception e) {
            return SCENARIO;
        }
    }
}
