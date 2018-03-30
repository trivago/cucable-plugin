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
    private CucableLogger logger;
    private Log mojoLogger;
    private CucablePlugin cucablePlugin;

    @Before
    public void setup() {
        logger = mock(CucableLogger.class);
        mojoLogger = mock(Log.class);
        logger.initialize(mojoLogger, "default");
        PropertyManager propertyManager = mock(PropertyManager.class);
        FileSystemManager fileSystemManager = mock(FileSystemManager.class);
        FeatureFileConverter featureFileConverter = mock(FeatureFileConverter.class);

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
        verify(logger, times(1)).initialize(mojoLogger, "default");
        verify(logger, times(3)).info(anyString(), any(CucableLogger.CucableLogLevel.class));
    }
}
