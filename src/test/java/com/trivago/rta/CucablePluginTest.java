package com.trivago.rta;

import com.trivago.rta.features.FeatureFileConverter;
import com.trivago.rta.files.FileSystemManager;
import com.trivago.rta.logging.CucableLogger;
import com.trivago.rta.properties.PropertyManager;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CucablePluginTest {

    private PropertyManager propertyManager;
    private FileSystemManager fileSystemManager;
    private FeatureFileConverter featureFileConverter;
    private CucableLogger logger;

    private CucablePlugin cucablePlugin;

    @Before
    public void setup() {
        logger = mock(CucableLogger.class);
        propertyManager = mock(PropertyManager.class);
        fileSystemManager = mock(FileSystemManager.class);
        featureFileConverter = mock(FeatureFileConverter.class);

        cucablePlugin = new CucablePlugin(
                propertyManager,
                fileSystemManager,
                featureFileConverter,
                logger
        );
    }

    @Test
    public void logInvocationTest() throws Exception {
        cucablePlugin.execute();
        verify(logger, times(1)).setMojoLogger(any(Log.class));
        verify(logger, times(3)).info(anyString());
    }
}
