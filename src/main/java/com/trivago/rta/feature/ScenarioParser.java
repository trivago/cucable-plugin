package com.trivago.rta.feature;

import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ast.GherkinDocument;

import javax.inject.Singleton;
import java.io.FileReader;

@Singleton
public class ScenarioParser {
    // Prepare Gherkin parser.
    private Parser<GherkinDocument> gherkinDocumentParser = new Parser<>(new AstBuilder());

    GherkinDocument parse(final FileReader fileReader) {
        return gherkinDocumentParser.parse(fileReader);
    }
}
