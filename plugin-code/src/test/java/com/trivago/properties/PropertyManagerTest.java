package com.trivago.properties;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.properties.WrongOrMissingPropertiesException;
import com.trivago.logging.CucableLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PropertyManagerTest {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
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
    public void customPlaceholdersTest() {
        Map<String, String> customPlaceholders = new HashMap<>();
        customPlaceholders.put("one", "two");
        customPlaceholders.put("three", "four");
        propertyManager.setCustomPlaceholders(customPlaceholders);
        assertThat(propertyManager.getCustomPlaceholders().size(), is(2));
        assertThat(propertyManager.getCustomPlaceholders().get("one"), is("two"));
        assertThat(propertyManager.getCustomPlaceholders().get("three"), is("four"));
    }

    @Test
    public void sourceRunnerTemplateFileTest() {
        propertyManager.setSourceRunnerTemplateFile("myTemplate");
        assertThat(propertyManager.getSourceRunnerTemplateFile(), is("myTemplate"));
    }

    @Test
    public void parallelizationModeTest() throws CucablePluginException {
        propertyManager.setParallelizationMode("features");
        assertThat(propertyManager.getParallelizationMode(), is(PropertyManager.ParallelizationMode.FEATURES));
        propertyManager.setParallelizationMode("scenarios");
        assertThat(propertyManager.getParallelizationMode(), is(PropertyManager.ParallelizationMode.SCENARIOS));
    }

    @Test
    public void wrongParallelizationModeTest() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("Unknown <parallelizationMode> 'unknown'. Please use 'scenarios' or 'features'.");
        propertyManager.setParallelizationMode("unknown");
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
        expectedException.expectMessage("Tag 'noAtInFront' of type 'include' does not start with '@'.");

        List<String> tags = new ArrayList<>();
        tags.add("noAtInFront");
        propertyManager.setIncludeScenarioTags(tags);
    }

    @Test
    public void wrongExcludeTagFormatTest() throws Exception {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("Tag 'noAtInFront' of type 'exclude' does not start with '@'.");

        List<String> tags = new ArrayList<>();
        tags.add("noAtInFront");
        propertyManager.setExcludeScenarioTags(tags);
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesScenariosMode() throws CucablePluginException {
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.SCENARIOS.toString());
        propertyManager.checkForDisallowedParallelizationModeProperties();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesSourceFeaturesIsNotDirectoryTest() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = FEATURE, sourceFeatures should point to a directory!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures("my.feature");
        propertyManager.checkForDisallowedParallelizationModeProperties();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesExcludeTagsSpecified() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = FEATURE, you cannot specify excludeScenarioTags!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        List<String> excludeTags = new ArrayList<>();
        excludeTags.add("@someTag");
        propertyManager.setExcludeScenarioTags(excludeTags);

        propertyManager.checkForDisallowedParallelizationModeProperties();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesIncludeTagsSpecified() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = FEATURE, you cannot specify includeScenarioTags!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        List<String> includeTags = new ArrayList<>();
        includeTags.add("@someTag");
        propertyManager.setIncludeScenarioTags(includeTags);

        propertyManager.checkForDisallowedParallelizationModeProperties();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesValid() throws CucablePluginException {
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        propertyManager.checkForDisallowedParallelizationModeProperties();
    }

    @Test
    public void logMandatoryPropertiesTest() {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        propertyManager.logProperties();
        verify(logger, times(6)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        List<String> capturedLogs = logCaptor.getAllValues();
        assertThat(capturedLogs.get(0), is("- sourceRunnerTemplateFile  : null"));
        assertThat(capturedLogs.get(1), is("- generatedRunnerDirectory  : null"));
        assertThat(capturedLogs.get(2), is("- sourceFeatures            : null"));
        assertThat(capturedLogs.get(3), is("- parallelizationMode       : null"));
        assertThat(capturedLogs.get(4), is("- generatedFeatureDirectory : null"));
        assertThat(capturedLogs.get(5), is("- numberOfTestRuns          : 0"));
    }

    @Test
    public void logExtendedPropertiesTest() throws CucablePluginException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);

        List<String> excludeScenarioTags = new ArrayList<>();
        excludeScenarioTags.add("@exclude1");
        excludeScenarioTags.add("@exclude2");
        propertyManager.setExcludeScenarioTags(excludeScenarioTags);

        List<String> includeScenarioTags = new ArrayList<>();
        includeScenarioTags.add("@include1");
        includeScenarioTags.add("@include2");
        propertyManager.setIncludeScenarioTags(includeScenarioTags);

        Map<String, String> customPlaceholders = new HashMap<>();
        customPlaceholders.put("key1", "value1");
        customPlaceholders.put("key2", "value2");
        propertyManager.setCustomPlaceholders(customPlaceholders);

        propertyManager.setSourceFeatures("test.feature:3");
        propertyManager.setDesiredNumberOfRunners(2);

        propertyManager.logProperties();

        verify(logger, times(13)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        List<String> capturedLogs = logCaptor.getAllValues();
        assertThat(capturedLogs.get(0), is("- sourceRunnerTemplateFile  : null"));
        assertThat(capturedLogs.get(1), is("- generatedRunnerDirectory  : null"));
        assertThat(capturedLogs.get(2), is("- sourceFeatures            : test.feature"));
        assertThat(capturedLogs.get(3), is("                              with line number 3"));
        assertThat(capturedLogs.get(4), is("- includeScenarioTags       : @include1, @include2"));
        assertThat(capturedLogs.get(5), is("- excludeScenarioTags       : @exclude1, @exclude2"));
        assertThat(capturedLogs.get(6), is("- parallelizationMode       : null"));
        assertThat(capturedLogs.get(7), is("- customPlaceholders        :"));
        assertThat(capturedLogs.get(8), is("  key1 => value1"));
        assertThat(capturedLogs.get(9), is("  key2 => value2"));
        assertThat(capturedLogs.get(10), is("- generatedFeatureDirectory : null"));
        assertThat(capturedLogs.get(11), is("- numberOfTestRuns          : 0"));
        assertThat(capturedLogs.get(12), is("- desiredNumberOfRunners    : 2"));
    }

    @Test
    public void logMissingPropertiesTest() throws CucablePluginException {
        expectedException.expect(WrongOrMissingPropertiesException.class);
        expectedException.expectMessage("Properties not specified correctly in the configuration section of your pom file: [<sourceRunnerTemplateFile>, <generatedRunnerDirectory>, <sourceFeatures>, <generatedFeatureDirectory>]");
        propertyManager.checkForMissingMandatoryProperties();

    }
}
