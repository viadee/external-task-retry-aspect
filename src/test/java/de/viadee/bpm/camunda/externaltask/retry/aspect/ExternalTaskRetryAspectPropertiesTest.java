package de.viadee.bpm.camunda.externaltask.retry.aspect;

import de.viadee.bpm.camunda.externaltask.retry.aspect.config.ExternalTaskRetryAspectProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExternalTaskRetryAspectPropertiesTest {

    @Test
    public void propertiesShouldNotBeNullTest() {
        ExternalTaskRetryAspectProperties properties = new ExternalTaskRetryAspectProperties();
        properties.setRetryConfigName(null);
        properties.setDefaultRetryConfig(null);
        assertEquals("R3/PT5M", properties.getDefaultRetryConfig());
        assertEquals("RETRY_CONFIG", properties.getRetryConfigName());
    }


    @Test
    public void propertiesShouldNotBeEmptyTest() {
        ExternalTaskRetryAspectProperties properties = new ExternalTaskRetryAspectProperties();
        properties.setRetryConfigName("  ");
        properties.setDefaultRetryConfig("  ");
        assertEquals("R3/PT5M", properties.getDefaultRetryConfig());
        assertEquals("RETRY_CONFIG", properties.getRetryConfigName());
    }
}
