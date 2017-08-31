package com.trivago.rta.features;

import com.trivago.rta.exceptions.filesystem.FeatureFileParseException;
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

    @Test(expected = FeatureFileParseException.class)
    public void testConvertInvalidPathListToSingleScenariosAndRunners() throws Exception {
        List<Path> pathList = new ArrayList<>();
        Path mockPath = getMockPath("dummyfeature.feature");
        pathList.add(mockPath);
        int counter = featureFileConverter.convertToSingleScenariosAndRunners(pathList);
        assertThat(counter, is(1));
    }

    private Path getMockPath(String filePath) {
        Path mockPath = mock(Path.class);
        Path mockFilePath = mock(Path.class);
        when(mockFilePath.toString()).thenReturn(filePath);
        when(mockPath.getFileName()).thenReturn(mockFilePath);
        when(mockPath.toString()).thenReturn(filePath);
        return mockPath;
    }
}
