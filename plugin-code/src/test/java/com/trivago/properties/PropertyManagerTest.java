package com.trivago.properties;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.MissingFileException;
import com.trivago.exceptions.properties.WrongOrMissingPropertiesException;
import com.trivago.files.FileIO;
import com.trivago.logging.CucableLogger;
import com.trivago.vo.CucableFeature;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PropertyManagerTest {
    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private PropertyManager propertyManager;
    private CucableLogger logger;
    private FileIO fileIO;

    @Before
    public void setup() {
        logger = mock(CucableLogger.class);
        fileIO = mock(FileIO.class);
        propertyManager = new PropertyManager(logger, fileIO);
    }

    @Test
    public void setGeneratedRunnerDirectoryTest() {
        propertyManager.setGeneratedRunnerDirectory("test");
        assertThat(propertyManager.getGeneratedRunnerDirectory(), is("test"));
    }

    @Test
    public void setGeneratedFeatureDirectoryTest() {
        propertyManager.setGeneratedFeatureDirectory("test");
        assertThat(propertyManager.getGeneratedFeatureDirectory(), is("test"));
    }

    @Test
    public void setNumberOfTestRunsTest() {
        propertyManager.setNumberOfTestRuns(11);
        assertThat(propertyManager.getNumberOfTestRuns(), is(11));
    }

    @Test
    public void setDesiredNumberOfRunnersTest() {
        propertyManager.setDesiredNumberOfRunners(12);
        assertThat(propertyManager.getDesiredNumberOfRunners(), is(12));
    }

    @Test
    public void setDesiredNumberOfFeaturesPerRunnerTest() {
        propertyManager.setDesiredNumberOfFeaturesPerRunner(5);
        assertThat(propertyManager.getDesiredNumberOfFeaturesPerRunner(), is(5));
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
    public void scenarioNamesTest() {
        List<String> expectedList = new ArrayList<>();
        expectedList.add("scenarioName1");
        expectedList.add("scenarioName2");
        propertyManager.setScenarioNames("scenarioName1, scenarioName2");

        assertThat(propertyManager.getScenarioNames(), is(expectedList));
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
    public void featureWithoutScenarioLineNumberTest() throws MissingFileException {
        propertyManager.setSourceFeatures("my.feature");
        List<CucableFeature> sourceFeatures = propertyManager.getSourceFeatures();
        assertThat(sourceFeatures.size(), is(1));
        assertThat(sourceFeatures.get(0).getName(), is("my.feature"));
        assertThat(sourceFeatures.get(0).getLineNumbers(), is(notNullValue()));
        assertThat(sourceFeatures.get(0).getLineNumbers().size(), is(0));
    }

    @Test
    public void featureTextFileTest() throws MissingFileException {
        when(fileIO.readContentFromFile("src/test/resources/features.txt")).thenCallRealMethod();
        propertyManager.setSourceFeatures("@src/test/resources/features.txt");
        List<CucableFeature> sourceFeatures = propertyManager.getSourceFeatures();
        assertThat(sourceFeatures.size(), is(2));
        assertThat(sourceFeatures.get(0).getName(), is("file:///features/feature1.feature"));
        assertThat(sourceFeatures.get(0).getLineNumbers(), is(notNullValue()));
        assertThat(sourceFeatures.get(0).getLineNumbers().size(), is(1));
        assertThat(sourceFeatures.get(0).getLineNumbers().get(0), is(12));
        assertThat(sourceFeatures.get(1).getName(), is("file:///features/feature2.feature"));
        assertThat(sourceFeatures.get(1).getLineNumbers(), is(notNullValue()));
        assertThat(sourceFeatures.get(1).getLineNumbers().size(), is(1));
        assertThat(sourceFeatures.get(1).getLineNumbers().get(0), is(25));
    }

    @Test
    public void featureWithScenarioLineNumberTest() throws MissingFileException {
        propertyManager.setSourceFeatures("my.feature:123");
        List<CucableFeature> sourceFeatures = propertyManager.getSourceFeatures();
        assertThat(sourceFeatures.size(), is(1));
        assertThat(sourceFeatures.get(0).getName(), is("my.feature"));
        assertThat(sourceFeatures.get(0).getLineNumbers().size(), is(1));
        assertThat(sourceFeatures.get(0).getLineNumbers().get(0), is(123));
    }

    @Test
    public void featureWithInvalidScenarioLineNumberTest() throws MissingFileException {
        propertyManager.setSourceFeatures("my.feature:abc");
        List<CucableFeature> sourceFeatures = propertyManager.getSourceFeatures();
        assertThat(sourceFeatures.size(), is(1));
        assertThat(sourceFeatures.get(0).getName(), is("my.feature:abc"));
        assertThat(sourceFeatures.get(0).getLineNumbers().size(), is(0));
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesScenariosMode() throws CucablePluginException {
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.SCENARIOS.toString());
        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesSourceFeaturesIsNotDirectoryTest() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = features, sourceFeatures should point to a directory!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures("my.feature");
        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesIncludeTagsSpecified() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = features, you cannot specify includeScenarioTags!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        propertyManager.setIncludeScenarioTags("someTag");

        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesScenarioNamesSpecified() throws CucablePluginException {
        expectedException.expect(CucablePluginException.class);
        expectedException.expectMessage("In parallelizationMode = features, you cannot specify scenarioNames!");

        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        propertyManager.setScenarioNames("name1");

        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test
    public void checkForDisallowedParallelizationModePropertiesValid() throws CucablePluginException {
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());
        propertyManager.setSourceFeatures(testFolder.getRoot().getPath());
        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test(expected = CucablePluginException.class)
    public void desiredNumberOfRunnersAndFeaturesPerRunnersTest() throws CucablePluginException {
        propertyManager.setDesiredNumberOfRunners(2);
        propertyManager.setDesiredNumberOfFeaturesPerRunner(3);
        propertyManager.checkForDisallowedPropertyCombinations();
    }

    @Test
    public void logMandatoryPropertiesTest() throws CucablePluginException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        propertyManager.setParallelizationMode("scenarios");
        propertyManager.logProperties();
        verify(logger, times(6)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        List<String> capturedLogs = logCaptor.getAllValues();
        assertThat(capturedLogs.get(0), is("- sourceFeatures:"));
        assertThat(capturedLogs.get(1), is("- sourceRunnerTemplateFile     : null"));
        assertThat(capturedLogs.get(2), is("- generatedRunnerDirectory     : null"));
        assertThat(capturedLogs.get(3), is("- generatedFeatureDirectory    : null"));
        assertThat(capturedLogs.get(4), is("- parallelizationMode          : scenarios"));
        assertThat(capturedLogs.get(5), is("- numberOfTestRuns             : 0"));
    }

    @Test
    public void logExtendedPropertiesTest() throws CucablePluginException {
        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        propertyManager.setIncludeScenarioTags("@include1 and @include2");

        Map<String, String> customPlaceholders = new HashMap<>();
        customPlaceholders.put("key1", "value1");
        customPlaceholders.put("key2", "value2");
        propertyManager.setCustomPlaceholders(customPlaceholders);

        propertyManager.setSourceFeatures("test.feature:3");
        propertyManager.setDesiredNumberOfRunners(2);
        propertyManager.setParallelizationMode("features");

        propertyManager.logProperties();

        verify(logger, times(12)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        List<String> capturedLogs = logCaptor.getAllValues();
        assertThat(capturedLogs.get(0), is("- sourceFeatures:"));
        assertThat(capturedLogs.get(1), is("  - test.feature (line 3)"));
        assertThat(capturedLogs.get(2), is("- sourceRunnerTemplateFile     : null"));
        assertThat(capturedLogs.get(3), is("- generatedRunnerDirectory     : null"));
        assertThat(capturedLogs.get(4), is("- generatedFeatureDirectory    : null"));
        assertThat(capturedLogs.get(5), is("- includeScenarioTags          : @include1 and @include2"));
        assertThat(capturedLogs.get(6), is("- customPlaceholders           :"));
        assertThat(capturedLogs.get(7), is("  key1 => value1"));
        assertThat(capturedLogs.get(8), is("  key2 => value2"));
        assertThat(capturedLogs.get(9), is("- parallelizationMode          : features"));
        assertThat(capturedLogs.get(10), is("- numberOfTestRuns             : 0"));
        assertThat(capturedLogs.get(11), is("- desiredNumberOfRunners       : 2"));
    }

    @Test
    public void logMissingPropertiesTest() throws CucablePluginException {
        expectedException.expect(WrongOrMissingPropertiesException.class);
        expectedException.expectMessage("Properties not specified correctly in the configuration section of your pom file: [<sourceFeatures>, <sourceRunnerTemplateFile>, <generatedRunnerDirectory>, <generatedFeatureDirectory>]");
        propertyManager.checkForMissingMandatoryProperties();

    }
}
