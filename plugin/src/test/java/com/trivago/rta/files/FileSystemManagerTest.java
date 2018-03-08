package com.trivago.rta.files;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.exceptions.filesystem.PathCreationException;
import com.trivago.rta.properties.PropertyManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSystemManagerTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private PropertyManager propertyManager;
    private FileSystemManager fileSystemManager;

    @Before
    public void setup() {
        propertyManager = mock(PropertyManager.class);
        fileSystemManager = new FileSystemManager(propertyManager);
    }

    @Test(expected = CucablePluginException.class)
    public void invalidSourceFeaturesTest() throws Exception {
        when(propertyManager.getSourceFeatures()).thenReturn("");
        fileSystemManager.getFeatureFilePaths();
    }

    @Test(expected = PathCreationException.class)
    public void prepareGeneratedFeatureAndRunnerDirsMissingFeatureDirTest() throws Exception {
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn("");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirs();
    }

    @Test(expected = PathCreationException.class)
    public void prepareGeneratedFeatureAndRunnerDirsMissingRunnerDirTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn(featurePath);
        when(propertyManager.getGeneratedRunnerDirectory()).thenReturn("");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirs();
    }

    @Test
    public void prepareGeneratedFeatureAndRunnerDirsTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        String runnerPath = testFolder.getRoot().getPath().concat("/runnerDir");
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn(featurePath);
        when(propertyManager.getGeneratedRunnerDirectory()).thenReturn(runnerPath);
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirs();
    }

    @Test(expected = NullPointerException.class)
    public void getFeatureFilePathsInvalidSourceFeaturesTest() throws Exception {
        fileSystemManager.getFeatureFilePaths();
    }

    @Test
    public void getFeatureFilePathsEmptySourceFeaturesTest() throws Exception {
        String sourceFeatures = testFolder.getRoot().getPath();
        when(propertyManager.getSourceFeatures()).thenReturn(sourceFeatures);
        List<Path> featureFilePaths = fileSystemManager.getFeatureFilePaths();
        assertThat(featureFilePaths, is(notNullValue()));
    }

}
