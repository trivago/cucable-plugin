package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
import com.trivago.rta.exceptions.filesystem.MissingFileException;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.ast.Background;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class GherkinDocumentParser {

    /**
     * Get a list of lists of keywords (one entry per scenario).
     * @param gherkinDocument {@link GherkinDocument}
     * @return A list of lists of keywords
     * @throws FeatureFileParseException
     * @throws MissingFileException
     */
    public List<List<String>> getKeywordsFromGherkinDocument(GherkinDocument gherkinDocument) throws FeatureFileParseException, MissingFileException {

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
                scenarioKeywords.add(stepKeywords);
            }
        }

        return scenarioKeywords;
    }

    public GherkinDocument getGherkinDocumentFromFeatureFile(final Path featureFilePath) throws MissingFileException, FeatureFileParseException {
        Parser<GherkinDocument> gherkinDocumentParser = new Parser<>(new AstBuilder());
        GherkinDocument gherkinDocument;
        try {
            FileReader fileReader = new FileReader(featureFilePath.toFile());
            gherkinDocument = gherkinDocumentParser.parse(fileReader);
        } catch (FileNotFoundException e) {
            throw new MissingFileException(featureFilePath.toString());
        } catch (ParserException parserException) {
            throw new FeatureFileParseException(featureFilePath.toString());
        }

        return gherkinDocument;
    }

    public  List<Pickle> getPicklesFromGherkinDocument(GherkinDocument gherkinDocument){
        return new Compiler().compile(gherkinDocument);
    }
}
