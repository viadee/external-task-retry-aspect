package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.BaseTest;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertEquals;


@TestPropertySource(properties = "de.viadee.bpm.camunda.external-task.retry-time-cycle-identifier=CUSTOM_SOMETHING")
public class CustomRetryTimeCycleIdentifierTest extends BaseTest {

    @Test
    public void customRetryTimeCycleIdentifier() {
        assertEquals("CUSTOM_SOMETHING", this.properties.getRetryTimeCycleIdentifier());
    }

}
