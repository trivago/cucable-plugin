package com.trivago.gherkin;

import com.trivago.vo.DataTable;
import com.trivago.vo.Step;
import io.cucumber.messages.types.Examples;
import io.cucumber.messages.types.Location;
import io.cucumber.messages.types.TableCell;
import io.cucumber.messages.types.TableRow;
import io.cucumber.messages.types.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.CoreMatchers.notNullValue;

class GherkinToCucableConverterTest {

    private GherkinToCucableConverter gherkinToCucableConverter;

    @Before
    public void setup() {
        gherkinToCucableConverter = new GherkinToCucableConverter();
    }

    @Test
    public void convertGherkinStepsToCucableStepsTest() {
        List<io.cucumber.messages.types.Step> gherkinSteps = new ArrayList<>();
        io.cucumber.messages.types.Step step1 = new io.cucumber.messages.types.Step(
                new Location(1L, 1L),
                "Given ",
                null,
                "I am on the homepage",
                null,
                null,
                "step-1"
        );
        io.cucumber.messages.types.Step step2 = new io.cucumber.messages.types.Step(
                new Location(2L, 1L),
                "When ",
                null,
                "I click on the login button",
                null,
                null,
                "step-2"
        );
        gherkinSteps.add(step1);
        gherkinSteps.add(step2);

        // When
        List<Step> result = gherkinToCucableConverter.convertGherkinStepsToCucableSteps(gherkinSteps);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getName(), is("Given I am on the homepage"));
        assertThat(result.get(1).getName(), is("When I click on the login button"));
    }

    @Test
    public void convertGherkinTagsToCucableTagsTest() {
        // Given
        List<Tag> gherkinTags = new ArrayList<>();
        Tag tag1 = new Tag(new Location(1L, 1L), "@smoke", "tag-1");
        Tag tag2 = new Tag(new Location(1L, 1L), "@regression", "tag-2");
        gherkinTags.add(tag1);
        gherkinTags.add(tag2);

        // When
        List<String> result = gherkinToCucableConverter.convertGherkinTagsToCucableTags(gherkinTags);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result.get(0), is("@smoke"));
        assertThat(result.get(1), is("@regression"));
    }

    @Test
    void convertGherkinDataTableToCucableDataTable() {
        // Given
        List<TableCell> headerCells = new ArrayList<>();
        headerCells.add(new TableCell(new Location(1L, 1L), "Name"));
        headerCells.add(new TableCell(new Location(1L, 1L), "Age"));
        headerCells.add(new TableCell(new Location(1L, 1L), "City"));
        TableRow headerRow = new TableRow(new Location(1L, 1L), headerCells, "header-row");

        List<TableCell> row1Cells = new ArrayList<>();
        row1Cells.add(new TableCell(new Location(2L, 1L), "John"));
        row1Cells.add(new TableCell(new Location(2L, 1L), "25"));
        row1Cells.add(new TableCell(new Location(2L, 1L), "New York"));
        TableRow row1 = new TableRow(new Location(2L, 1L), row1Cells, "row-1");

        List<TableCell> row2Cells = new ArrayList<>();
        row2Cells.add(new TableCell(new Location(3L, 1L), "Jane"));
        row2Cells.add(new TableCell(new Location(3L, 1L), "30"));
        row2Cells.add(new TableCell(new Location(3L, 1L), "London"));
        TableRow row2 = new TableRow(new Location(3L, 1L), row2Cells, "row-2");

        List<TableRow> rows = new ArrayList<>();
        rows.add(headerRow);
        rows.add(row1);
        rows.add(row2);

        io.cucumber.messages.types.DataTable gherkinDataTable = new io.cucumber.messages.types.DataTable(new Location(1L, 1L), rows);

        // When
        DataTable result = gherkinToCucableConverter.convertGherkinDataTableToCucableDataTable(gherkinDataTable);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getRows().size(), is(3));
        assertThat(result.getRows().get(0).get(0), is("Name"));
        assertThat(result.getRows().get(0).get(1), is("Age"));
        assertThat(result.getRows().get(0).get(2), is("City"));
        assertThat(result.getRows().get(1).get(0), is("John"));
        assertThat(result.getRows().get(1).get(1), is("25"));
        assertThat(result.getRows().get(1).get(2), is("New York"));
        assertThat(result.getRows().get(2).get(0), is("Jane"));
        assertThat(result.getRows().get(2).get(1), is("30"));
        assertThat(result.getRows().get(2).get(2), is("London"));
    }

    @Test
    void convertGherkinExampleTableToCucableExampleMap() {
        // Given
        List<TableCell> headerCells = new ArrayList<>();
        headerCells.add(new TableCell(new Location(1L, 1L), "Name"));
        headerCells.add(new TableCell(new Location(1L, 1L), "Age"));
        TableRow headerRow = new TableRow(new Location(1L, 1L), headerCells, "header-row");

        List<TableCell> row1Cells = new ArrayList<>();
        row1Cells.add(new TableCell(new Location(2L, 1L), "John"));
        row1Cells.add(new TableCell(new Location(2L, 1L), "25"));
        TableRow row1 = new TableRow(new Location(2L, 1L), row1Cells, "row-1");

        List<TableCell> row2Cells = new ArrayList<>();
        row2Cells.add(new TableCell(new Location(3L, 1L), "Jane"));
        row2Cells.add(new TableCell(new Location(3L, 1L), "30"));
        TableRow row2 = new TableRow(new Location(3L, 1L), row2Cells, "row-2");

        List<TableRow> bodyRows = new ArrayList<>();
        bodyRows.add(row1);
        bodyRows.add(row2);

        Examples examples = new Examples(
                new Location(1L, 1L),
                new ArrayList<>(),
                "Examples:",
                "User Examples",
                "",
                headerRow,
                bodyRows,
                "examples-1"
        );

        // When
        Map<String, List<String>> result = gherkinToCucableConverter.convertGherkinExampleTableToCucableExampleMap(examples);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.size(), is(2));
        assertThat(result.keySet().toArray()[0], is("<Name>"));
        assertThat(result.keySet().toArray()[1], is("<Age>"));
        assertThat(result.get("<Name>").get(0), is("John"));
        assertThat(result.get("<Age>").get(0), is("25"));
        assertThat(result.get("<Name>").get(1), is("Jane"));
        assertThat(result.get("<Age>").get(1), is("30"));
    }
}
