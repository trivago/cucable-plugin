package com.trivago.files;

import com.trivago.exceptions.CucablePluginException;
import com.trivago.exceptions.filesystem.FileCreationException;
import com.trivago.exceptions.filesystem.MissingFileException;
import com.trivago.exceptions.filesystem.PathCreationException;
import com.trivago.properties.PropertyManager;
import com.trivago.vo.CucableFeature;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSystemManagerTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private FileSystemManager fileSystemManager;

    @Before
    public void setup() {
        fileSystemManager = new FileSystemManager();
    }

    @Test(expected = PathCreationException.class)
    public void prepareGeneratedFeatureAndRunnerDirsMissingFeatureDirTest() throws Exception {
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories("", "");
    }

    @Test
    public void prepareGeneratedFeatureAndRunnerDirsMissingRunnerDirTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories("", featurePath);
    }

    @Test
    public void prepareGeneratedFeatureAndRunnerDirsTest() throws Exception {
        String featurePath = testFolder.getRoot().getPath().concat("/featureDir");
        String runnerPath = testFolder.getRoot().getPath().concat("/runnerDir");
        fileSystemManager.prepareGeneratedFeatureAndRunnerDirectories(runnerPath, featurePath);
    }

    @Test
    public void getPathsFromCucableFeatureNullTest() throws CucablePluginException {
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(null);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(0));
    }

    @Test(expected = CucablePluginException.class)
    public void getPathsFromCucableFeatureInvalidFeatureTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("", "","name.feature", null);
        fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
    }

    @Test
    public void getPathsFromCucableFeatureValidEmptyPathTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("", "",testFolder.getRoot().getPath(), null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(0));
    }

    @Test
    public void getPathsFromCucableFeatureFullPathTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("", "","src/test/resources", null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(2));
    }

    @Test
    public void getPathsFromCucableFeatureValidFeatureTest() throws CucablePluginException {
        CucableFeature cucableFeatures = new CucableFeature("", "","src/test/resources/feature1.feature", null);
        List<Path> pathsFromCucableFeature = fileSystemManager.getPathsFromCucableFeature(cucableFeatures);
        assertThat(pathsFromCucableFeature, is(notNullValue()));
        assertThat(pathsFromCucableFeature.size(), is(1));
    }

    @Test(expected = FileCreationException.class)
    public void writeToInvalidFileTest() throws Exception {
        fileSystemManager.writeContentToFile(null, "");
    }

    @Test
    public void fileReadWriteTest() throws Exception {
        String testString = "This is a test!";
        String path = testFolder.getRoot().getPath().concat("/test.tmp");
        fileSystemManager.writeContentToFile(testString, path);
        assertThat(fileSystemManager.readContentFromFile(path), Is.is(testString));
    }

    @Test(expected = MissingFileException.class)
    public void readFromMissingFileTest() throws Exception {
        String wrongPath = testFolder.getRoot().getPath().concat("/missing.tmp");
        fileSystemManager.readContentFromFile(wrongPath);
    }
}
