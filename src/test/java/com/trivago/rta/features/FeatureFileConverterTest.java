package com.trivago.rta.features;

import com.trivago.rta.files.FeatureFileContentRenderer;
import com.trivago.rta.files.FileWriter;
import com.trivago.rta.files.RunnerFileContentRenderer;
import com.trivago.rta.properties.PropertyManager;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeatureFileConverterTest {

    FeatureFileConverter featureFileConverter;

    @Before
    public void setup() {

        PropertyManager propertyManager = mock(PropertyManager.class);
        GherkinDocumentParser gherkinDocumentParser = mock(GherkinDocumentParser.class);
        FeatureFileContentRenderer featureFileContentRenderer = mock(FeatureFileContentRenderer.class);
        RunnerFileContentRenderer runnerFileContentRenderer = mock(RunnerFileContentRenderer.class);
        FileWriter fileWriter = mock(FileWriter.class);

        featureFileConverter = new FeatureFileConverter(
                propertyManager,
                gherkinDocumentParser,
                featureFileContentRenderer,
                runnerFileContentRenderer,
                fileWriter
        );
    }

    @Test
    public void testConvertPathListToSingleScenariosAndRunners() throws Exception {
        List<Path> pathList = new ArrayList<>();

        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn("dummyfeature.feature");
        when(mockPath.getFileName()).thenReturn(mockFilePath);
        pathList.add(mockPath);

        int counter = featureFileConverter.convertToSingleScenariosAndRunners(pathList);
        assertThat(counter, is (1));
    }

    @Test
    public void testConvertPathToSingleScenariosAndRunners() throws Exception {
        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn("dummyfeature.feature");
        when(mockPath.getFileName()).thenReturn(mockFilePath);

        featureFileConverter.convertToSingleScenariosAndRunners(mockPath);
    }

}
