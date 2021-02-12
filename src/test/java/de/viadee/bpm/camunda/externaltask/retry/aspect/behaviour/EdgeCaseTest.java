package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.BaseTest;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


public class EdgeCaseTest extends BaseTest {

    @Test
    public void testMethod() {
        try {
            this.externalTaskRetryAspect.externalTaskHandlerExecute(null, null);

        } catch (Exception exception) {
            fail("should not fail");
        }
    }


    @Test
    public void negativeRetriesTest() {
        // prepare
        when(this.externalTask.getRetries()).thenReturn(-1); // however

        // test
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new RuntimeException(), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertRemainingRetries(0);
    }


    @Test
    public void runtimeException() {
        // prepare
        when(this.externalTask.getRetries()).thenReturn(null);
        when(this.externalTask
                .getExtensionProperty(this.properties.getRetryTimeCycleIdentifier()))
                    .thenReturn("P,P,P"); // sadly, 'P' is matched by the used regex currently -> use hard-wired Fallback 'PT10M' then

        // test
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new RuntimeException(), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertRemainingRetries(3);
        this.assertNextRetryInterval(5 * MINUTES_TO_MILLIS);
    }

}
