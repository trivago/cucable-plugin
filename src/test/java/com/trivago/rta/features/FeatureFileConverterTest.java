package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.MissingFileException;
import com.trivago.rta.files.FeatureFileContentRenderer;
import com.trivago.rta.files.FileIO;
import com.trivago.rta.files.RunnerFileContentRenderer;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureFileConverterTest {

    private FeatureFileConverter featureFileConverter;

    @Before
    public void setup() {

        PropertyManager propertyManager = mock(PropertyManager.class);
        GherkinDocumentParser gherkinDocumentParser = mock(GherkinDocumentParser.class);
        FeatureFileContentRenderer featureFileContentRenderer = mock(FeatureFileContentRenderer.class);
        RunnerFileContentRenderer runnerFileContentRenderer = mock(RunnerFileContentRenderer.class);
        FileIO fileIO = mock(FileIO.class);
        CucableLogger logger = mock(CucableLogger.class);

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
    public void testConvertmptyPathListToSingleScenariosAndRunners() throws Exception {
        List<Path> pathList = new ArrayList<>();
        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn("");
        when(mockPath.getFileName()).thenReturn(mockFilePath);
        when(mockPath.toString()).thenReturn("");
        pathList.add(mockPath);
        featureFileConverter.convertToSingleScenariosAndRunners(pathList);
    }
}
