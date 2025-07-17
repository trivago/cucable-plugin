package com.trivago.logging;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CucableLoggerTest {

    private CucableLogger logger;

    @Before
    public void setup() {
        logger = mock(CucableLogger.class);
    }

    @Test
    public void infoTest() {
        logger.initialize("default");
        logger.info("Test");
        verify(logger, times(1))
                .info("Test");
    }

    @Test
    public void infoNoLogLevelTest() {
        logger.initialize(null);
        logger.info("Test");
        verify(logger, times(1))
                .info("Test");
    }

    @Test
    public void illegalCucableLogLevelShouldBeDefaultTest() {
        logger.initialize("something");
        logger.info("Test", CucableLogger.CucableLogLevel.DEFAULT);
        verify(logger, times(1))
                .info("Test", CucableLogger.CucableLogLevel.DEFAULT);
    }

    @Test
    public void logInfoSeparatorTest() {
        logger.initialize("default");
        logger.logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);
        verify(logger, times(1))
                .logInfoSeparator(CucableLogger.CucableLogLevel.DEFAULT);
    }
}
