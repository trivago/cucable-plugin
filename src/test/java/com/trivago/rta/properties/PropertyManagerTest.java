package com.trivago.rta.properties;

import com.trivago.rta.logging.CucableLogger;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PropertyManagerTest {
    private PropertyManager propertyManager;

    @Before
    public void setup() {
        CucableLogger logger = mock(CucableLogger.class);
        propertyManager = new PropertyManager(logger);
    }

    @Test
    public void testFeatureWithoutScenarioLineNumber() {
        propertyManager.setSourceFeatures("my.feature");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature"));
        assertThat(propertyManager.getScenarioLineNumber(), is(nullValue()));
    }

    @Test
    public void testFeatureWithScenarioLineNumber() {
        propertyManager.setSourceFeatures("my.feature:123");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature"));
        assertThat(propertyManager.getScenarioLineNumber(), is(123));
    }

    @Test
    public void testFeatureWithInvalidScenarioLineNumber() {
        propertyManager.setSourceFeatures("my.feature:abc");
        assertThat(propertyManager.getSourceFeatures(), is("my.feature:abc"));
        assertThat(propertyManager.getScenarioLineNumber(), is(nullValue()));
    }
}
