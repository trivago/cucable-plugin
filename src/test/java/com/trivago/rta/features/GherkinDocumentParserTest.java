package com.trivago.rta.features;

import com.trivago.rta.files.FileIO;
import gherkin.ast.Comment;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Location;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.ast.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GherkinDocumentParserTest {

    private GherkinDocumentParser gherkinDocumentParser;

    @Before
    public void setup() {
        FileIO fileIO = mock(FileIO.class);
        gherkinDocumentParser = new GherkinDocumentParser(fileIO);
    }

    @Test
    public void oneKeywordScenario() throws Exception {
        ScenarioDefinition mockScenario = mock(ScenarioDefinition.class);

        List<Step> steps = new ArrayList<>();
        Step step1 = new Step(new Location(1, 1), "given ", "this is step 1", null);
        steps.add(step1);
        when(mockScenario.getSteps()).thenReturn(steps);
        GherkinDocument gherkinDocument = getGherkinDocument(mockScenario);

        List<List<String>> keywordsFromGherkinDocument = gherkinDocumentParser.getKeywordsFromGherkinDocument(gherkinDocument);
        assertThat(keywordsFromGherkinDocument.size(), is(1));
        assertThat(keywordsFromGherkinDocument.get(0).size(), is(1));
        assertThat(keywordsFromGherkinDocument.get(0).get(0), is("given"));
    }

    @Test
    public void twoKeywordScenario() throws Exception {
        ScenarioDefinition mockScenario = mock(ScenarioDefinition.class);

        List<Step> steps = new ArrayList<>();
        Step step1 = new Step(new Location(1, 1), "given ", "this is step 1", null);
        steps.add(step1);
        Step step2 = new Step(new Location(2, 1), "then ", "this is step 2", null);
        steps.add(step2);
        when(mockScenario.getSteps()).thenReturn(steps);
        GherkinDocument gherkinDocument = getGherkinDocument(mockScenario);

        List<List<String>> keywordsFromGherkinDocument = gherkinDocumentParser.getKeywordsFromGherkinDocument(gherkinDocument);
        assertThat(keywordsFromGherkinDocument.size(), is(1));
        assertThat(keywordsFromGherkinDocument.get(0).size(), is(2));
        assertThat(keywordsFromGherkinDocument.get(0).get(0), is("given"));
        assertThat(keywordsFromGherkinDocument.get(0).get(1), is("then"));
    }

    private GherkinDocument getGherkinDocument(final ScenarioDefinition scenarioDefinition) {
        List<Tag> tags = new ArrayList<>();
        Location location = new Location(11, 22);
        String language = "language";
        String keyword = "keyword";
        String name = "name";
        String description = "description";

        List<ScenarioDefinition> children = new ArrayList<>();
        children.add(scenarioDefinition);

        Feature feature = new Feature(tags, location, language, keyword, name, description, children);
        List<Comment> comments = new ArrayList<>();
        return new GherkinDocument(feature, comments);
    }
}
