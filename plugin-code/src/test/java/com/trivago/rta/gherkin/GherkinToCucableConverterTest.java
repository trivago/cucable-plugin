package com.trivago.rta.gherkin;

import gherkin.ast.Examples;
import gherkin.ast.Location;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        assertThat(secondStep.getName(), is("Then I get a test result"));
    }

    @Test
    public void convertGherkinExampleTableToCucableExampleMapTest() {
        Location location = new Location(1, 2);
        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag(location, "@tag");
        tags.add(tag);
        String keyword = "keyword";
        String name = "name";
        String description = "description";

        List<TableCell> headerCells = new ArrayList<>();
        headerCells.add(new TableCell(location, "headerCell1"));
        headerCells.add(new TableCell(location, "headerCell2"));
        headerCells.add(new TableCell(location, "headerCell3"));
        TableRow tableHeader = new TableRow(location, headerCells);

        List<TableRow> tableBody = new ArrayList<>();
        List<TableCell> bodyCells = new ArrayList<>();
        bodyCells.add(new TableCell(location, "bodyCell1"));
        bodyCells.add(new TableCell(location, "bodyCell2"));
        bodyCells.add(new TableCell(location, "bodyCell3"));
        tableBody.add(new TableRow(location, bodyCells));

        bodyCells = new ArrayList<>();
        bodyCells.add(new TableCell(location, "bodyCell4"));
        bodyCells.add(new TableCell(location, "bodyCell5"));
        bodyCells.add(new TableCell(location, "bodyCell6"));
        tableBody.add(new TableRow(location, bodyCells));

        Examples examples = new Examples(location, tags, keyword, name, description, tableHeader, tableBody);
        List<String> includeTags = new ArrayList<>();
        List<String> excludeTags = new ArrayList<>();
        Map<String, List<String>> table =
                gherkinToCucableConverter.convertGherkinExampleTableToCucableExampleMap(examples);

        assertThat(table.size(), is(3));
    }
}
