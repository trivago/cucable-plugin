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

package com.trivago.rta.logging;

import org.apache.maven.plugin.logging.Log;

import javax.inject.Singleton;

@Singleton
public class CucableLogger {

    private Log mojoLogger;

    /**
     * Set the mojo logger so it can be used in any class that injects a CucableLogger.
     *
     * @param mojoLogger The current {@link Log}.
     */
    public void setMojoLogger(final Log mojoLogger) {
        this.mojoLogger = mojoLogger;
    }

    /**
     * Info logging.
     *
     * @param logString The {@link String} to be logged.
     */
    public void info(final CharSequence logString) {
        mojoLogger.info(logString);
    }
}
