package com.trivago.rta.gherkin;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.vo.SingleScenario;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class GherkinDocumentParserTest {

    private GherkinDocumentParser gherkinDocumentParser;

    @Before
    public void setup() {
        GherkinToCucableConverter gherkinToCucableConverter = new GherkinToCucableConverter();
        gherkinDocumentParser = new GherkinDocumentParser(gherkinToCucableConverter);
    }

    @Test(expected = CucablePluginException.class)
    public void invalidFeatureTest() throws Exception {
        gherkinDocumentParser.getSingleScenariosFromFeature("", null);
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("This is a scenario with two steps"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("This is a scenario with two steps"));
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, null);
        assertThat(singleScenariosFromFeature.size(), is(1));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("This is a scenario with background"));
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

        List<SingleScenario> singleScenariosFromFeature = gherkinDocumentParser.getSingleScenariosFromFeature(featureContent, null);
        assertThat(singleScenariosFromFeature.size(), is(2));

        SingleScenario scenario = singleScenariosFromFeature.get(0);

        assertThat(scenario.getScenarioName(), is("This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 1"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'one'"));

        scenario = singleScenariosFromFeature.get(1);

        assertThat(scenario.getScenarioName(), is("This is a scenario outline"));
        assertThat(scenario.getSteps().size(), is(2));
        assertThat(scenario.getBackgroundSteps().size(), is(0));
        assertThat(scenario.getSteps().get(0).getDataTable(), is(nullValue()));

        assertThat(scenario.getSteps().get(0).getName(), is("When I search for key 2"));
        assertThat(scenario.getSteps().get(1).getName(), is("Then I see the value 'two'"));
    }
}
