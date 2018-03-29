package com.trivago.rta.properties;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.logging.CucableLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PropertyManagerTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PropertyManager propertyManager;
    private CucableLogger logger;

    @Before
    public void setup() {
        logger = mock(CucableLogger.class);
        propertyManager = new PropertyManager(logger);
    }

    @Test
    public void featureWithoutScenarioLineNumberTest() {
        propertyManager.setSourceFeatures("my.feature");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature"));
        assertThat(propertyManager.getScenarioLineNumbers(), is(notNullValue()));
        assertThat(propertyManager.getScenarioLineNumbers().size(), is(0));
    }

    @Test
    public void featureWithScenarioLineNumberTest() {
        propertyManager.setSourceFeatures("my.feature:123");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature"));
        assertThat(propertyManager.getScenarioLineNumbers().size(), is(1));
        assertThat(propertyManager.getScenarioLineNumbers().get(0), is(123));
    }

    @Test
    public void featureWithInvalidScenarioLineNumberTest() {
        propertyManager.setSourceFeatures("my.feature:abc");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature:abc"));
        assertThat(propertyManager.getScenarioLineNumbers(), is(notNullValue()));
        assertThat(propertyManager.getScenarioLineNumbers().size(), is(0));
    }

    @Test
    public void wrongIncludeTagFormatTest() throws Exception {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("Include tag 'noAtInFront' does not start with '@'.");

        propertyManager.setSourceFeatures("-");
        propertyManager.setSourceRunnerTemplateFile("-");
        propertyManager.setGeneratedFeatureDirectory("-");
        propertyManager.setGeneratedRunnerDirectory("-");
        List<String> tags = new ArrayList<>();
        tags.add("noAtInFront");
        propertyManager.setIncludeScenarioTags(tags);
        propertyManager.validateSettings();
    }

    @Test
    public void wrongExcludeTagFormatTest() throws Exception {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("Exclude tag 'noAtInFront' does not start with '@'.");

        propertyManager.setSourceFeatures("-");
        propertyManager.setSourceRunnerTemplateFile("-");
        propertyManager.setGeneratedFeatureDirectory("-");
        propertyManager.setGeneratedRunnerDirectory("-");
        List<String> tags = new ArrayList<>();
        tags.add("noAtInFront");
        propertyManager.setExcludeScenarioTags(tags);
        propertyManager.validateSettings();
    }

    @Test
    public void logMandatoryPropertiesTest() {
        propertyManager.logProperties();
        verify(logger, times(1)).info("- sourceRunnerTemplateFile  : null", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- generatedRunnerDirectory  : null", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- sourceFeature(s)          : null", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- generatedFeatureDirectory : null", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- numberOfTestRuns          : 0", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
    }

    @Test
    public void logExtendedPropertiesTest() {
        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("exclude1");
        excludeScenarioTags.add("exclude2");
        propertyManager.setExcludeScenarioTags(excludeScenarioTags);

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("include1");
        includeScenarioTags.add("include2");
        propertyManager.setIncludeScenarioTags(includeScenarioTags);

        propertyManager.setSourceFeatures("test.feature:3");

        propertyManager.logProperties();
        verify(logger, times(1)).info("- sourceFeature(s)          : test.feature", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("                              with line number(s) [3]", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- include scenario tag(s)   : include1, include2", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
        verify(logger, times(1)).info("- exclude scenario tag(s)   : exclude1, exclude2", CucableLogger.CucableLogLevel.DEFAULT, CucableLogger.CucableLogLevel.COMPACT);
    }
}
