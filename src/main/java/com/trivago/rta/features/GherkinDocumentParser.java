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

package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FileIO;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GherkinDocumentParser {

    private final FileIO fileIO;

    @Inject
    public GherkinDocumentParser(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    /**
     * Get a list of lists of keywords (one entry per scenario).
     *
     * @param gherkinDocument {@link GherkinDocument}
     * @return A list of lists of keywords
     * @throws FeatureFileParseException in case a feature cannot be parsed.
     * @throws MissingFileException      in case a requested feature file does not exist.
     */
    List<List<String>> getKeywordsFromGherkinDocument(GherkinDocument gherkinDocument) throws FeatureFileParseException, MissingFileException {

        // Store keywords ("Given", "When", "Then", "And") for each step in the current scenario
        List<List<String>> scenarioKeywords = new ArrayList<>();
        List<String> stepKeywords;
        List<String> backgroundKeywords;

        Feature feature = gherkinDocument.getFeature();
        backgroundKeywords = new ArrayList<>();

        for (ScenarioDefinition scenario : feature.getChildren()) {
            if (scenario instanceof Background) {
                // Save background steps in order to add them to
                // all scenarios inside the same feature files
                scenario.getSteps().stream().map(step -> step.getKeyword().trim()).forEach(backgroundKeywords::add);
            } else {
                stepKeywords = new ArrayList<>();
                stepKeywords.addAll(backgroundKeywords);
                scenario.getSteps().stream().map(step -> step.getKeyword().trim()).forEach(stepKeywords::add);

                // Ignore scenarios without steps
                if (stepKeywords.size() > 0) {
                    scenarioKeywords.add(stepKeywords);
                }
            }
        }

        return scenarioKeywords;
    }

    GherkinDocument getGherkinDocumentFromFeatureFile(final Path featureFilePath) throws MissingFileException, FeatureFileParseException {
        Parser<GherkinDocument> gherkinDocumentParser = new Parser<>(new AstBuilder());
        GherkinDocument gherkinDocument;
        try {
            String content = fileIO.readContentFromFile(featureFilePath.toString());
            gherkinDocument = gherkinDocumentParser.parse(content);
        } catch (ParserException parserException) {
            throw new FeatureFileParseException(featureFilePath.toString());
        }

        return gherkinDocument;
    }

    List<Pickle> getPicklesFromGherkinDocument(GherkinDocument gherkinDocument) {
        return new Compiler().compile(gherkinDocument);
    }
}
