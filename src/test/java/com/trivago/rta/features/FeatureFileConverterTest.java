package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FileIO;
import com.trivago.rta.gherkin.GherkinDocumentParser;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import com.trivago.rta.runners.RunnerFileContentRenderer;
import com.trivago.rta.vo.SingleScenario;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureFileConverterTest {

    private FeatureFileConverter featureFileConverter;
    private FileIO fileIO;
    private PropertyManager propertyManager;
    private GherkinDocumentParser gherkinDocumentParser;
    private FeatureFileContentRenderer featureFileContentRenderer;

    @Before
    public void setup() {

        gherkinDocumentParser = mock(GherkinDocumentParser.class);
        featureFileContentRenderer = mock(FeatureFileContentRenderer.class);
        RunnerFileContentRenderer runnerFileContentRenderer = mock(RunnerFileContentRenderer.class);
        fileIO = mock(FileIO.class);
        CucableLogger logger = mock(CucableLogger.class);
        propertyManager = new PropertyManager(logger);

        featureFileConverter = new FeatureFileConverter(
                propertyManager,
                gherkinDocumentParser,
                featureFileContentRenderer,
                runnerFileContentRenderer,
                fileIO,
                logger
        );
    }

    @Test(expected = MissingFileException.class)
    public void testConvertEmptyPathListToSingleScenariosAndRunners() throws Exception {
        List<Path> pathList = new ArrayList<>();
        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn("");
        when(mockPath.getFileName()).thenReturn(mockFilePath);
        when(mockPath.toString()).thenReturn("");
        pathList.add(mockPath);
        featureFileConverter.convertToSingleScenariosAndRunners(pathList);
    }

    @Test
    public void testConvertToSingleScenariosAndRunners() throws Exception {
        // TODO use mocked file system to verify generated runners and features

        propertyManager.setNumberOfTestRuns(1);
        propertyManager.setGeneratedFeatureDirectory("");
        propertyManager.setGeneratedRunnerDirectory("");

        when(fileIO.readContentFromFile("TEST_PATH")).thenReturn("TEST_CONTENT");

        List<SingleScenario> scenarioList = new ArrayList<>();
        SingleScenario singleScenario = new SingleScenario("feature", "name", new ArrayList<>(), new ArrayList<>());
        scenarioList.add(singleScenario);
        when(gherkinDocumentParser.getSingleScenariosFromFeature("TEST_CONTENT", null)).thenReturn(scenarioList);

        String featureFileContent = "test";
        when(featureFileContentRenderer.getRenderedFeatureFileContent(singleScenario)).thenReturn(featureFileContent);

        List<Path> pathList = new ArrayList<>();
        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn("FEATURE_FILE.feature");
        when(mockPath.getFileName()).thenReturn(mockFilePath);
        when(mockPath.toString()).thenReturn("TEST_PATH");
        pathList.add(mockPath);
        featureFileConverter.convertToSingleScenariosAndRunners(pathList);
    }
}
