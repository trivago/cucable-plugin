package com.trivago.rta.logging;

import com.trivago.rta.properties.PropertyManager;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static com.trivago.rta.logging.CucableLogger.CucableLogLevel.COMPACT;
import static com.trivago.rta.logging.CucableLogger.CucableLogLevel.DEFAULT;
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
    }

    @Test
    public void infoTest() {
        logger.initialize(mockedLogger, "default");
        logger.info("Test");
        verify(mockedLogger, times(1))
                .info("Test");
    }

    @Test
    public void infoNoLogLevelTest() {
        logger.initialize(mockedLogger, null);
        logger.info("Test");
        verify(mockedLogger, times(1))
                .info("Test");
    }

    @Test
    public void illegalCucableLogLevelShouldBeDefaultTest() {
        logger.initialize(mockedLogger, "something");
        logger.info("Test", DEFAULT);
        verify(mockedLogger, times(1))
                .info("Test");
    }

    @Test
    public void infoCompactLogLevelTest() {
        logger.initialize(mockedLogger, "compact");
        logger.info("Test", DEFAULT);
        verify(mockedLogger, times(0))
                .info("Test");
        logger.info("Test2", COMPACT);
        verify(mockedLogger, times(1))
                .info("Test2");
    }
}
