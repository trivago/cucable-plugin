package com.trivago.rta.logging;

import org.apache.maven.plugin.logging.Log;

import javax.inject.Singleton;

@Singleton
public class CucableLogger {

    private Log mojoLogger;

    public void setMojoLogger(final Log mojoLogger) {
        this.mojoLogger = mojoLogger;
    }

    public boolean isDebugEnabled() {
        return mojoLogger.isDebugEnabled();
    }

    public void debug(final CharSequence charSequence) {
        mojoLogger.debug(charSequence);
    }

    public void debug(final CharSequence charSequence, final Throwable throwable) {
        mojoLogger.debug(charSequence, throwable);
    }

    public void debug(final Throwable throwable) {
        mojoLogger.debug(throwable);
    }

    public boolean isInfoEnabled() {
        return mojoLogger.isInfoEnabled();
    }

    public void info(final CharSequence charSequence) {
        mojoLogger.info(charSequence);
    }

    public void info(final CharSequence charSequence, final Throwable throwable) {
        mojoLogger.info(charSequence, throwable);
    }

    public void info(final Throwable throwable) {
        mojoLogger.info(throwable);
    }

    public boolean isWarnEnabled() {
        return mojoLogger.isWarnEnabled();
    }

    public void warn(final CharSequence charSequence) {
        mojoLogger.warn(charSequence);
    }

    public void warn(final CharSequence charSequence, final Throwable throwable) {
        mojoLogger.warn(charSequence, throwable);
    }

    public void warn(final Throwable throwable) {
        mojoLogger.warn(throwable);
    }

    public boolean isErrorEnabled() {
        return mojoLogger.isErrorEnabled();
    }

    public void error(final CharSequence charSequence) {
        mojoLogger.error(charSequence);
    }

    public void error(final CharSequence charSequence, final Throwable throwable) {
        mojoLogger.error(charSequence, throwable);
    }

    public void error(final Throwable throwable) {
        mojoLogger.error(throwable);
    }
}
