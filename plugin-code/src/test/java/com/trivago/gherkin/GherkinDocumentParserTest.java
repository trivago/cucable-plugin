package com.trivago.gherkin;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.DataTable;
import com.trivago.vo.SingleScenario;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GherkinDocumentParserTest {

    private GherkinDocumentParser gherkinDocumentParser;
    private PropertyManager propertyManager;

    @Before
    public void setup() {
        GherkinToCucableConverter gherkinToCucableConverter = new GherkinToCucableConverter();
        GherkinTranslations gherkinTranslations = new GherkinTranslations();
        propertyManager = mock(PropertyManager.class);
        gherkinDocumentParser = new GherkinDocumentParser(gherkinToCucableConverter, gherkinTranslations, propertyManager);
    }

    @Test(expected = CucablePluginException.class)
    public void invalidFeatureTest() throws Exception {
        gherkinDocumentParser.getSingleScenariosFromFeature("", "");
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("Scenario: This is a scenario with two steps"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
    }

    @Test
    public void validFeatureOneIncludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@tag1");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureTwoIncludeTagsTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@tag1");
        includeScenarioTags.add("@tag3");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test
    public void validFeatureTwoIncludeTagsWithAndConnectorTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@tag1");
        includeScenarioTags.add("@tag2");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);
        when(propertyManager.getIncludeScenarioTagsConnector()).thenReturn(PropertyManager.TagConnectMode.AND);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureTwoExcludeTagsWithAndConnectorTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@tag1");
        excludeScenarioTags.add("@tag2");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(excludeScenarioTags);
        when(propertyManager.getExcludeScenarioTagsConnector()).thenReturn(PropertyManager.TagConnectMode.AND);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneExcludeTagNoScenarioTagsTest() throws Exception {
        String featureContent = "@featureTag\n" +
                "Feature: test feature\n" +
                "\n" +
                "Scenario: scenario 1";

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@tag1");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(excludeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneIncludeTagNoScenarioTagsTest() throws Exception {
        String featureContent = "@featureTag\n" +
                "Feature: test feature\n" +
                "\n" +
                "Scenario: scenario 1";

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@tag1");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(0));
    }

    @Test
    public void validFeatureOneExcludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@tag1");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(excludeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureOneExcludeOneIncludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@tag1");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(excludeScenarioTags);
        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@tag3");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validScenarioExcludeTagOverridesIncludeTagTest() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> tags = new ArrayList<>();
        tags.add("@tag1");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(tags);
        when(propertyManager.getIncludeScenarioTags()).thenReturn(tags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
    }

    @Test
    public void validFeatureTagIsConsideredInIncludeTags() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@featureTag");
        when(propertyManager.getIncludeScenarioTags()).thenReturn(includeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(2));
    }

    @Test
    public void validFeatureTagIsConsideredInExcludeTags() throws Exception {
        String featureContent = getTwoScenariosWithTags();

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@featureTag");
        when(propertyManager.getExcludeScenarioTags()).thenReturn(excludeScenarioTags);

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(0));
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(2));

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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        String stepName = singleScenariosFromFeature.get(1).getSteps().get(0).getName();
        assertThat(stepName, is("Given this is a step with 23 and two\\nthree"));
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
        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
        assertThat(singleScenariosFromFeature.size(), is(1));
        assertThat(singleScenariosFromFeature.get(0).getSteps().size(), is(1));

        DataTable dataTable = singleScenariosFromFeature.get(0).getSteps().get(0).getDataTable();

        assertThat(dataTable.getRows().size(), is(1));
        List<String> firstRow = dataTable.getRows().get(0);
        assertThat(firstRow.get(0), is("test"));
        assertThat(firstRow.get(1), is("1"));
        assertThat(firstRow.get(2), is("one"));
    }

    @Test(expected = IllegalStateException.class)
    public void nonexistentPlaceholderInOutlineNameTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                "\n" +
                "  Scenario Outline: This is a scenario outline with <nonexistentPlaceholder>\n" +
                "    When I search for key <key>\n" +
                "    | test | <key> | <value> |" +
                "\n" +
                "    Examples:\n" +
                "      | key | value |\n" +
                "      | 1   | one   |\n";
        gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
    }

    @Test(expected = IllegalStateException.class)
    public void nonexistentPlaceholderInOutlineStepTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                "\n" +
                "  Scenario Outline: This is a scenario outline\n" +
                "    When I search for key <nonexistentplaceholder>\n" +
                "    | test | <key> | <value> |" +
                "\n" +
                "    Examples:\n" +
                "      | key | value |\n" +
                "      | 1   | one   |\n";
        gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
    }

    @Test(expected = IllegalStateException.class)
    public void nonexistentPlaceholderInOutlineDataTableTest() throws Exception {
        String featureContent = "Feature: test feature 3\n" +
                "\n" +
                "  Scenario Outline: This is a scenario outline\n" +
                "    When I search for key \n" +
                "    | test | <nonexistentplaceholder> | <value> |" +
                "\n" +
                "    Examples:\n" +
                "      | key | value |\n" +
                "      | 1   | one   |\n";
        gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, "");
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
}
