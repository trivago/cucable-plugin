package com.trivago.rta.logging;

import com.trivago.rta.properties.PropertyManager;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CucableLoggerTest {

    private Log mockedLogger;
    private CucableLogger logger;

    @Before
    public void setup() {
        mockedLogger = mock(Log.class);
        PropertyManager propertyManager = mock(PropertyManager.class);
        logger = new CucableLogger();
        logger.initialize(mockedLogger, "default");
    }

    @Test
    public void infoTest() {
        logger.log("Test");
        verify(mockedLogger, times(1))
                .info("Test");
    }
}
