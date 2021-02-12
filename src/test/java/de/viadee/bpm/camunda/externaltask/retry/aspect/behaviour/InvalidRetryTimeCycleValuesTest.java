package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.BaseTest;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.when;


@TestPropertySource(properties = "de.viadee.bpm.camunda.external-task.default-retry-time-cycle=R3/PT37M")
public class InvalidRetryTimeCycleValuesTest extends BaseTest {


    @Test
    public void invalidRetryTimeCycle() {
        // PT3D not valid -> 'default-retry-time-cycle' should be used
        this.invalidTimeCycleDetectedByRegularExpression("R3/PT3D");
    }


    @Test
    public void invalidRetryTimeCycleList() {
        // PT3D not valid -> 'default-retry-time-cycle' should be used
        this.invalidTimeCycleDetectedByRegularExpression("PT10M,PT3D,PT10M");
    }


    public void invalidTimeCycleDetectedByRegularExpression(final String retryTimeCycle) {
        // prepare
        when(this.externalTask.getRetries()).thenReturn(3);
        when(this.externalTask.getExtensionProperty(this.properties.getRetryTimeCycleIdentifier())).thenReturn(retryTimeCycle);

        // test
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new RuntimeException(), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertRemainingRetries(2);
        this.assertNextRetryInterval(37 * MINUTES_TO_MILLIS);
    }

}
