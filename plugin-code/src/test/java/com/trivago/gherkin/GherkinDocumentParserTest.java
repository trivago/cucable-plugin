package com.trivago.gherkin;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.logging.CucableLogger;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.DataTable;
import com.trivago.vo.SingleScenario;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GherkinDocumentParserTest {

    private GherkinDocumentParser gherkinDocumentParser;
    private PropertyManager propertyManager;
    private CucableLogger mockedLogger;

    @Before
    public void setup() {
        GherkinToCucableConverter gherkinToCucableConverter = new GherkinToCucableConverter();
        propertyManager = mock(PropertyManager.class);
        mockedLogger = mock(CucableLogger.class);
        gherkinDocumentParser =
                new GherkinDocumentParser(gherkinToCucableConverter, propertyManager, mockedLogger);
    }

    @Test
    public void invalidFeatureTest() throws Exception {
        gherkinDocumentParser.getSingleScenariosFromFeature("", "", null);
        verify(mockedLogger, times(1)).warn("No parsable gherkin.");
    }

    @Test
    public void validFeatureTest() throws Exception {
        String featureContent = "@featureTag\n" +
                                "Feature: test feature\n" +
                                "\n" +
                                "@scenario1Tag1\n" +
                                "@scenario1Tag2\n" +
                                "Scenario: This is a scenario with two steps\n" +
                                "Given this is step 1\n" +
                                "Then this is step 2\n";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario with two steps"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
    }

    @Test
    public void validFeatureOneIncludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1");

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneScenarioNameTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList("scenario 1"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneScenarioNameNonEnglishTest() throws Exception {
        String featureContent = getTwoScenariosNonEnglish();

        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList("Mulțumesc"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test(expected = CucablePluginException.class)
    public void invalidFeatureOneIncludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();
        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1 wrongOperator @tag2");
        gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
    }

    @Test
    public void validFeatureTwoIncludeTagsTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1 or @tag3");

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test
    public void validFeatureTwoScenarioNamesTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getScenarioNames()).thenReturn(Arrays.asList("scenario 1", "scenario 2"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test
    public void validFeatureTwoIncludeTagsOneScenarioNameTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1 or @tag3");
        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList("scenario 1"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureMatchingIncludeTagsNoMatchingScenarioNameTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1 or @tag3");
        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList("scenario 3"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(0));
    }

    @Test
    public void validFeatureNoMatchingIncludeTagsMatchingScenarioNameTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag4");
        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList("scenario 1"));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(0));
    }

    @Test
    public void validFeatureEmptyScenarioNameListTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getScenarioNames()).thenReturn(Collections.singletonList(""));

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test
    public void validFeatureTwoIncludeTagsWithAndConnectorTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1 and @tag2");

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneIncludeTagNoScenarioTagsTest() throws Exception {
        String featureContent = "@featureTag\n" +
                                "Feature: test feature\n" +
                                "\n" +
                                "Scenario: scenario 1";

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@tag1");

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(0));
    }

    @Test
    public void validFeatureTagIsConsideredInIncludeTags() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        when(propertyManager.getIncludeScenarioTags()).thenReturn("@featureTag");

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test(expected = CucablePluginException.class)
    public void parseErrorTest() throws Exception {
        String featureContent = "&/ASD";
        gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
    }

    @Test
    public void validFeatureWithDataTableTest() throws Exception {
        String featureContent = "@featureTag\n" +
                                "Feature: test feature\n" +
                                "\n" +
                                "@scenario1Tag1\n" +
                                "@scenario1Tag2\n" +
                                "Scenario: This is a scenario with two steps\n" +
                                "Given this is step 1\n" +
                                "|value1|value2|\n" +
                                "Then this is step 2\n";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario with two steps"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(notNullValue()));
        assertThat(scenario.getSteps().get(0).getDataTable().getRows().size(), is(1));
        assertThat(scenario.getSteps().get(0).getDataTable().getRows().get(0).size(), is(2));
    }

    @Test
    public void validFeatureWithBackgroundScenarioTest() throws Exception {
        String featureContent = "Feature: FeatureName\n" +
                                "\n" +
                                "  Background:\n" +
                                "    Given BackgroundGivenStep\n" +
                                "    And BackgroundGivenStep2\n" +
                                "\n" +
                                "  @tag1\n" +
                                "  @tag2\n" +
                                "  Scenario: This is a scenario with background\n" +
                                "    Then ThenStep";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario with background"));
        assertThat(scenario.getSteps().size(), is(1));
        assertThat(scenario.getBackgroundSteps().size(), is(2));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));
    }

    @Test
    public void validFeatureWithScenarioOutlineTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario outline\n" +
                                "    When I search for key <key>\n" +
                                "    Then I see the value '<value>'\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n" +
                                "      | 2   | two   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario Outline: This is a scenario outline"));
        assertThat(scenario.isScenarioOutline(), is(true));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key <key>"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value '<value>'"));
        
        assertThat(scenario.getExampleHeaders(), is(Arrays.asList("key", "value")));
        assertThat(scenario.getExampleRow(), is(Arrays.asList("1", "one")));

        scenario = singleScenariosFromFeature.get(1);

        assertThat(scenario.getScenarioName(), is("Scenario Outline: This is a scenario outline"));
        assertThat(scenario.isScenarioOutline(), is(true));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key <key>"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value '<value>'"));
        
        assertThat(scenario.getExampleHeaders(), is(Arrays.asList("key", "value")));
        assertThat(scenario.getExampleRow(), is(Arrays.asList("2", "two")));
    }

    @Test
    public void validFeatureWithScenarioOutlineAndTwoExampleTablesTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario outline\n" +
                                "    When I search for key <key>\n" +
                                "    Then I see the value '<value>'\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n" +
                                "      | 2   | two   |\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | uno   |\n" +
                                "      | 2   | dos   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(4));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 1"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'one'"));

        scenario = singleScenariosFromFeature.get(1);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 2"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'two'"));

        scenario = singleScenariosFromFeature.get(2);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 1"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'uno'"));

        scenario = singleScenariosFromFeature.get(3);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 2"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'dos'"));
    }

    @Test
    public void validScenarioNamesWithScenarioOutlineTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario outline, key = <key>, value = <value>\n" +
                                "    This is a step\n" +
                                "    How about another step\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n" +
                                "      | 2   | two   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(2));

        SingleScenario scenario = singleScenariosFromFeature.get(0);
        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline, key = 1, value = one"));
        scenario = singleScenariosFromFeature.get(1);
        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario outline, key = 2, value = two"));
    }

    @Test
    public void validScenarioWithLineBreakInExampleTableTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario outline\n" +
                                "    Given this is a step with <key> and <value>\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n" +
                                "      | 23   | two\\nthree   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        String stepName = singleScenariosFromFeature.get(1).getSteps().get(0).getName();
        assertThat(stepName, is("Given this is a step with 23 and two\\nthree"));
    }

    @Test
    public void replacePlaceholderInStringTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario with <key> and <value>!\n" +
                                "    Given this is a step\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n";
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(
                singleScenariosFromFeature.get(0).getScenarioName(),
                is("Scenario: This is a scenario with 1 and one!")
        );
    }

    @Test
    public void replacePlaceholderInStringWithMissingPlaceholdersTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario with <key> and <value>!\n" +
                                "    Given this is a step\n" +
                                "\n" +
                                "    Examples:\n" +
                                "      | someKey | someValue |\n" +
                                "      | 1       | one       |\n";
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(
                singleScenariosFromFeature.get(0).getScenarioName(),
                is("Scenario: This is a scenario with <key> and <value>!")
        );
    }

    @Test
    public void replaceDataTableExamplePlaceholderTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                                "\n" +
                                "  Scenario Outline: This is a scenario outline\n" +
                                "    When I search for key <key>\n" +
                                "    | test | <key> | <value> |" +
                                "\n" +
                                "    Examples:\n" +
                                "      | key | value |\n" +
                                "      | 1   | one   |\n";
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
        assertThat(singleScenariosFromFeature.get(0).getSteps().size(), is(1));

        DataTable dataTable = singleScenariosFromFeature.get(0).getSteps().get(0).getDataTable();

        assertThat(dataTable.getRows().size(), is(1));
        List<String> firstRow = dataTable.getRows().get(0);
        assertThat(firstRow.get(0), is("test"));
        assertThat(firstRow.get(1), is("1"));
        assertThat(firstRow.get(2), is("one"));
    }

    @Test
    public void taggedFeatureAndExamplesTest() throws Exception {
        String featureContent = getScenarioWithFeatureAndExampleTags();
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(3));
        assertThat(singleScenariosFromFeature.get(0).getSteps().size(), is(2));
        assertThat(singleScenariosFromFeature.get(1).getSteps().size(), is(2));
        assertThat(singleScenariosFromFeature.get(2).getSteps().size(), is(2));

    }

    @Test
    public void taggedFeatureAndExamplesRequestedExampleTagTest() throws Exception {
        String featureContent = getScenarioWithFeatureAndExampleTags();
        when(propertyManager.getIncludeScenarioTags()).thenReturn("@exampleTag1");
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
        assertThat(singleScenariosFromFeature.get(0).getSteps().size(), is(2));
    }

    @Test
    public void taggedFeatureAndExamplesRequestedFeatureAndExampleTagTest() throws Exception {
        String featureContent = getScenarioWithFeatureAndExampleTags();
        when(propertyManager.getIncludeScenarioTags()).thenReturn("@featureTag and @exampleTag1");
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
        assertThat(singleScenariosFromFeature.get(0).getSteps().size(), is(2));
    }

    @Test
    public void taggedFeatureAndExamplesRequestedInvalidExampleTagTest() throws Exception {
        String featureContent = getScenarioWithFeatureAndExampleTags();
        when(propertyManager.getIncludeScenarioTags()).thenReturn("@exampleTag1 and @exampleTag2");
        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(0));
    }

    @Test
    public void scenarioOutlineWithOneExampleRowTest() throws Exception {
        String featureContent = "Feature: test feature\n" +
                "\n" +
                "  Scenario Outline: Outline with one row\n" +
                "    Given a step with <param>\n" +
                "\n" +
                "    Examples:\n" +
                "      | param |\n" +
                "      | foo   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(1));
        SingleScenario scenario = singleScenariosFromFeature.get(0);
        assertThat(scenario.getScenarioName(), is("Scenario Outline: Outline with one row"));
        assertThat(scenario.isScenarioOutline(), is(true));
        assertThat(scenario.getSteps().get(0).getName(), is("Given a step with <param>"));
        assertThat(scenario.getExampleHeaders(), is(Arrays.asList("param")));
        assertThat(scenario.getExampleRow(), is(Arrays.asList("foo")));
    }

    @Test
    public void scenarioAndScenarioOutlineInOneFeatureTest() throws Exception {
        String featureContent = "Feature: test feature\n" +
                "\n" +
                "  Scenario: Just a scenario\n" +
                "    Given a simple step\n" +
                "\n" +
                "  Scenario Outline: Outline\n" +
                "    Given a step with <param>\n" +
                "\n" +
                "    Examples:\n" +
                "      | param |\n" +
                "      | foo   |\n" +
                "      | bar   |";

        List<SingleScenario> singleScenariosFromFeature =
                gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "", null);
        assertThat(singleScenariosFromFeature.size(), is(3));
        // First: scenario
        SingleScenario scenario = singleScenariosFromFeature.get(0);
        assertThat(scenario.getScenarioName(), is("Scenario: Just a scenario"));
        assertThat(scenario.isScenarioOutline(), is(false));
        assertThat(scenario.getSteps().get(0).getName(), is("Given a simple step"));
        // Second: scenario outline, first row
        scenario = singleScenariosFromFeature.get(1);
        assertThat(scenario.getScenarioName(), is("Scenario Outline: Outline"));
        assertThat(scenario.isScenarioOutline(), is(true));
        assertThat(scenario.getExampleRow(), is(Arrays.asList("foo")));
        // Third: scenario outline, second row
        scenario = singleScenariosFromFeature.get(2);
        assertThat(scenario.getScenarioName(), is("Scenario Outline: Outline"));
        assertThat(scenario.isScenarioOutline(), is(true));
        assertThat(scenario.getExampleRow(), is(Arrays.asList("bar")));
    }

    private String getScenarioWithFeatureAndExampleTags() {
        return "@featureTag\n" +
               "Feature: test feature 3\n" +
               "\n" +
               "  Scenario Outline: This is a scenario outline\n" +
               "    When I search for key <key>\n" +
               "    Then I get <value>\n" +
               "\n" +
               "@exampleTag1\n" +
               "Examples:\n" +
               "      | key | value   |\n" +
               "      | 1   | one     |\n" +
               "@exampleTag2\n" +
               "Examples:\n" +
               "      | key | value   |\n" +
               "      | 2   | two     |\n" +
               "@exampleTag3\n" +
               "Examples:\n" +
               "      | key | value   |\n" +
               "      | 3   | three   |\n";
    }

    private String getTwoScenariosWithTags() {
        return "@featureTag\n" +
               "Feature: test feature\n" +
               "\n" +
               "@tag1\n" +
               "@tag2\n" +
               "Scenario: scenario 1\n" +
               "\n" +
               "@tag2\n" +
               "@tag3\n" +
               "Scenario: scenario 2";
    }

    private String getTwoScenariosNonEnglish() {
        return "#language: ro\n" +
               "Funcţionalitate: Bună ziua\n" +
               "\n" +
               "Scenariu: Mulțumesc foarte mult\n" +
               "\n" +
               "Scenariu: Mulțumesc foarte mult 2";
    }
}
