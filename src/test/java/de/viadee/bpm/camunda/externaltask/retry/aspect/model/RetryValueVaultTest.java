package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RetryValueVaultTest {

    @Test
    public void fallbackValuesTest() {
        RetryValueVault valueVault = new RetryValueVault(null, null);
        assertEquals(valueVault.getFallbackRetryTimeCycle(), valueVault.getDefaultRetryConfig());
        assertNull(valueVault.getRetryConfigIdentifier());
    }
}
