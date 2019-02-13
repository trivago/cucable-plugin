package com.trivago.files;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.PathCreationException;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.CucableFeature;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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

    @Test(expected = PathCreationException.class)
    public void prepareGeneratedFeatureAndRunnerDirsMissingFeatureDirTest() throws Exception {
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn("");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories();
    }

    @Test(expected = PathCreationException.class)
    public void prepareGeneratedFeatureAndRunnerDirsMissingRunnerDirTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn(featurePath);
        when(propertyManager.getGeneratedRunnerDirectory()).thenReturn("");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories();
    }

    @Test
    public void prepareGeneratedFeatureAndRunnerDirsTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        String runnerPath = testFolder.getRoot().getPath().concat("/runnerDir");
        when(propertyManager.getGeneratedFeatureDirectory()).thenReturn(featurePath);
        when(propertyManager.getGeneratedRunnerDirectory()).thenReturn(runnerPath);
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories();
    }

    @Test
    public void getPathsFromCucableFeatureNullTest() throws CucablePluginException {
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(null);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(0));
    }

    @Test(expected = CucablePluginException.class)
    public void getPathsFromCucableFeatureInvalidFeatureTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("name.feature", null);
        fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
    }

    @Test
    public void getPathsFromCucableFeatureValidEmptyPathTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature(testFolder.getRoot().getPath(), null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(0));
    }

    @Test
    public void getPathsFromCucableFeatureFullPathTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("src/test/resources", null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(2));
    }

    @Test
    public void getPathsFromCucableFeatureValidFeatureTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("src/test/resources/feature1.feature", null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(1));
    }
}
