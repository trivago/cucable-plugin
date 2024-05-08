/*
 * Copyright 2017 trivago N.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trivago.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class CucableLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(CucableLogger.class);

    private CucableLogLevel currentLogLevel;

    /**
     * Set the Log level.
     *
     * @param currentLogLevel the log level that the logger should react to.
     */
    public void initialize(final String currentLogLevel) {
        if (currentLogLevel == null) {
            this.currentLogLevel = CucableLogger.CucableLogLevel.DEFAULT;
            return;
        }

        try {
            this.currentLogLevel = CucableLogger.CucableLogLevel.valueOf(currentLogLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.currentLogLevel = CucableLogger.CucableLogLevel.DEFAULT;
            warn("Log level " + currentLogLevel + " is unknown. Cucable will use 'default' logging.");
        }
    }

    public void logInfoSeparator(final CucableLogLevel... cucableLogLevels) {
        info("--------------------------------------", cucableLogLevels);
    }

    /**
     * Info logging based on the provided Cucable log levels.
     *
     * @param logString        The {@link String} to be logged.
     * @param cucableLogLevels The log levels ({@link CucableLogLevel} list) in which the message should be displayed.
     */
    public void info(final String logString, CucableLogLevel... cucableLogLevels) {
        log(LogLevel.INFO, logString, cucableLogLevels);
    }

    /**
     * Warn logging. This is always displayed unless logging is off.
     *
     * @param logString The {@link String} to be logged.
     */
    public void warn(final String logString) {
        CucableLogLevel[] logLevels =
                new CucableLogLevel[]{CucableLogLevel.DEFAULT, CucableLogLevel.COMPACT, CucableLogLevel.MINIMAL};
        log(LogLevel.WARN, logString, logLevels);
    }

    /**
     * Logs a message based on the provided log levels.
     *
     * @param logString        The {@link String} to be logged.
     * @param cucableLogLevels The log levels ({@link CucableLogLevel} list) in which the message should be displayed.
     */
    private void log(final LogLevel logLevel, final String logString, CucableLogLevel... cucableLogLevels) {

        if (currentLogLevel != null
            && cucableLogLevels != null
            && cucableLogLevels.length > 0
            && Arrays.stream(cucableLogLevels).noneMatch(cucableLogLevel -> cucableLogLevel == currentLogLevel)) {
            return;
        }
        switch (logLevel) {
            case INFO:
                LOGGER.info(logString);
                break;
            case WARN:
                LOGGER.warn(logString);
                break;
        }
    }

    private enum LogLevel {
        INFO, WARN
    }

    public enum CucableLogLevel {
        DEFAULT, COMPACT, MINIMAL, OFF
    }
}
