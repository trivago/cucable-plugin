package com.trivago.features;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.FileCreationException;
import com.trivago.exceptions.filesystem.MissingFileException;
import com.trivago.files.FileIO;
import com.trivago.files.FileSystemManager;
import com.trivago.gherkin.GherkinDocumentParser;
import com.trivago.logging.CucableLogger;
import com.trivago.properties.PropertyManager;
import com.trivago.runners.RunnerFileContentRenderer;
import com.trivago.vo.CucableFeature;
import com.trivago.vo.FeatureRunner;
import com.trivago.vo.SingleScenario;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeatureFileConverterTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private FeatureFileConverter featureFileConverter;
    private FileIO fileIO;
    private PropertyManager propertyManager;
    private GherkinDocumentParser gherkinDocumentParser;
    private FeatureFileContentRenderer featureFileContentRenderer;
    private RunnerFileContentRenderer runnerFileContentRenderer;
    private CucableLogger logger;

    private FileSystemManager fileSystemManager;

    @Before
    public void setup() throws MissingFileException, FileCreationException {
        gherkinDocumentParser = mock(GherkinDocumentParser.class);
        featureFileContentRenderer = mock(FeatureFileContentRenderer.class);
        runnerFileContentRenderer = mock(RunnerFileContentRenderer.class);
        fileIO = mock(FileIO.class);
        fileSystemManager = mock(FileSystemManager.class);
        logger = mock(CucableLogger.class);
        propertyManager = new PropertyManager(logger);

        featureFileConverter = new FeatureFileConverter(
                propertyManager,
                gherkinDocumentParser,
                featureFileContentRenderer,
                runnerFileContentRenderer,
                fileIO,
                fileSystemManager,
                logger
        );
    }

    @Test
    public void generateParallelizableFeaturesEmptyFeaturesTest() throws CucablePluginException {
        List<CucableFeature> cucableFeatures = new ArrayList<>();
        featureFileConverter.generateParallelizableFeatures(cucableFeatures);
    }

    @Test
    public void convertEmptyPathListToSingleScenariosAndRunnersTest() throws Exception {
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.SCENARIOS.toString());
        List<CucableFeature> cucableFeatures = new ArrayList<>();
        cucableFeatures.add(new CucableFeature("", null));
        featureFileConverter.generateParallelizableFeatures(cucableFeatures);
    }

    @Test
    public void invalidLineNumberTest() throws Exception {
        List<CucableFeature> cucableFeatures = new ArrayList<>();
        cucableFeatures.add(new CucableFeature("FEATURE_FILE.feature", Collections.singletonList(2)));
        featureFileConverter.generateParallelizableFeatures(cucableFeatures);
    }

    @Test
    public void convertToSingleScenariosAndRunnersTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);
        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");
        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        assertThat(logCaptor.getAllValues().get(0), is("Cucable created 1 separate feature file and 1 runner."));
        verify(fileIO, times(2)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndRunnersWithScenarioNameTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");
        String scenarioMatchText = "Scenario: scenarioName1";

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";
        final String GENERATED_FEATURE_FILE_NAME = "FEATURE_FILE_scenario001_run001_IT.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setScenarioNames("scenarioName1");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME)).thenReturn(scenarioMatchText);

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioMatchText)).thenReturn(0);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);
        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        assertThat(logCaptor.getAllValues().get(0), is("Cucable created 1 separate feature file and 1 runner."));
        verify(fileIO, times(2)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndRunnersWithFeaturesModeTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setParallelizationMode("features");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);

        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");


        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        verify(fileIO, times(2)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndMultiRunnersTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setDesiredNumberOfRunners(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setParallelizationMode("scenarios");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);

        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        verify(fileIO, times(3)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndMultiRunnersWithScenarioNamesTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");
        final String scenarioMatch1Text = "Scenario: scenarioName1";
        final String scenarioMatch2Text = "Scenario: scenarioName2";

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";
        final String GENERATED_FEATURE_FILE_NAME1 = "FEATURE_FILE_scenario001_run001_IT.feature";
        final String GENERATED_FEATURE_FILE_NAME2 = "FEATURE_FILE_scenario002_run001_IT.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setParallelizationMode("scenarios");
        propertyManager.setScenarioNames("scenarioName1, scenarioName2");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME1)).thenReturn(scenarioMatch1Text);
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME2)).thenReturn(scenarioMatch2Text);

        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioMatch1Text)).thenReturn(0);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioMatch2Text)).thenReturn(1);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);

        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        assertThat(logCaptor.getAllValues().get(0), is("Cucable created 2 separate feature files and 2 runners."));
        verify(fileIO, times(4)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndMultiRunnersWithScenarioNamesAndExampleKeywordTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");
        final String scenarioMatch1Text = "Scenario: scenarioName1";
        final String scenarioMatch2Text = "Example: scenarioName2";

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";
        final String GENERATED_FEATURE_FILE_NAME1 = "FEATURE_FILE_scenario001_run001_IT.feature";
        final String GENERATED_FEATURE_FILE_NAME2 = "FEATURE_FILE_scenario002_run001_IT.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setParallelizationMode("scenarios");
        propertyManager.setScenarioNames("scenarioName1, scenarioName2");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME1)).thenReturn(scenarioMatch1Text);
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME2)).thenReturn(scenarioMatch2Text);

        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioMatch1Text)).thenReturn(0);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioMatch2Text)).thenReturn(1);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);

        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
        verify(logger, times(1)).info(logCaptor.capture(), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class), any(CucableLogger.CucableLogLevel.class));
        assertThat(logCaptor.getAllValues().get(0), is("Cucable created 2 separate feature files and 2 runners."));
        verify(fileIO, times(4)).writeContentToFile(anyString(), anyString());
    }

    @Test
    public void convertToSingleScenariosAndMultiRunnersFeaturesModeTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setDesiredNumberOfRunners(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setParallelizationMode(PropertyManager.ParallelizationMode.FEATURES.toString());

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);

        verify(fileIO, times(2)).writeContentToFile(anyString(), anyString());
    }

    @Test(expected = CucablePluginException.class)
    public void noScenariosMatchingScenarioNamesTest() throws Exception {
        String generatedFeatureDir = testFolder.getRoot().getPath().concat("/features/");
        String generatedRunnerDir = testFolder.getRoot().getPath().concat("/runners/");

        final String FEATURE_FILE_NAME = "FEATURE_FILE.feature";
        final String GENERATED_FEATURE_FILE_NAME = "FEATURE_FILE_scenario001_run001_IT.feature";
        final String scenarioNoMatchText = "Scenario: noMatch";

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory(generatedFeatureDir);
        propertyManager.setGeneratedRunnerDirectory(generatedRunnerDir);
        propertyManager.setScenarioNames("scenarioName1");

        when(fileIO.readContentFromFile(FEATURE_FILE_NAME)).thenReturn("TEST_CONTENT");
        when(fileIO.readContentFromFile(generatedFeatureDir + "/" + GENERATED_FEATURE_FILE_NAME)).thenReturn(scenarioNoMatchText);

        List<CucableFeature> cucableFeatures = new ArrayList<>();
        CucableFeature cucableFeature = new CucableFeature(FEATURE_FILE_NAME, null);
        cucableFeatures.add(cucableFeature);

        when(fileSystemManager.getPathsFromCucableFeature(cucableFeature)).thenReturn(Collections.singletonList(Paths.get(cucableFeature.getName())));

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = getSingleScenario();
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", FEATURE_FILE_NAME, null)).thenReturn(scenarioList);
        when(gherkinDocumentParser.matchScenarioWithScenarioNames(scenarioNoMatchText)).thenReturn(-1);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);
        when(runnerFileContentRenderer.getRenderedRunnerFileContent(any(FeatureRunner.class))).thenReturn("RUNNER_CONTENT");

        featureFileConverter.generateParallelizableFeatures(cucableFeatures);
    }

    private SingleScenario getSingleScenario() {
        return new SingleScenario(
                "feature", "", "",
                "featureDescription", "name",
                "scenarioDescription", new ArrayList<>(), new ArrayList<>());
    }
}
