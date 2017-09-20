package com.trivago.rta.files;

import com.trivago.rta.exceptions.CucablePluginException;
import com.trivago.rta.properties.PropertyManager;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileSystemManagerTest {
    private PropertyManager propertyManager;
    private FileSystemManager fileSystemManager;

    @Before
    public void setup(){
        propertyManager = mock(PropertyManager.class);
        fileSystemManager = new FileSystemManager(propertyManager);
    }

    @Test(expected = CucablePluginException.class)
    public void invalidSourceFeaturesTest() throws Exception {
        when(propertyManager.getSourceFeatures()).thenReturn("");
        fileSystemManager.getFeatureFilePaths();
    }
}
