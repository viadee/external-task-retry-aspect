package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.BaseTest;
import de.viadee.bpm.camunda.externaltask.retry.aspect.error.ExternalTaskBusinessError;
import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class DefaultBehaviourForExceptionTypesTest extends BaseTest {


    @Test
    public void runtimeException() {
        // prepare
        String errorMessage = "error-test-message";
        RuntimeException runtimeException = new RuntimeException(errorMessage);
        when(this.externalTask.getRetries()).thenReturn(null); // 1st try

        // test
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, runtimeException, this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage("RuntimeException: " + errorMessage);
        this.assertErrorDetails(runtimeException);
        this.assertRemainingRetries(3);
        this.assertNextRetryInterval(5 * MINUTES_TO_MILLIS);
    }


    @Test
    public void instantIncidentExceptionWithRootCause() {
        final RuntimeException rootCause = new RuntimeException("root-cause");

        this.externalTaskRetryAspect
                .handleErrorAfterThrown(this.joinPoint,
                        new InstantIncidentException("no-retries", rootCause),
                        this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage(rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage());
        this.assertErrorDetails(rootCause);
        this.assertNextRetryInterval(0);
        this.assertNoRemainingRetries();
    }


    @Test
    public void emptyInstantIncidentExceptionWithRootCause() {

        this.externalTaskRetryAspect
                .handleErrorAfterThrown(this.joinPoint,
                        new InstantIncidentException(),
                        this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage("InstantIncidentException: null");
        //this.assertErrorDetails(new InstantIncidentException());
        this.assertNextRetryInterval(0);
        this.assertNoRemainingRetries();
    }


    @Test
    public void emptyRootInstantIncidentException() {
        InstantIncidentException instantIncidentException = new InstantIncidentException(new RuntimeException());
        this.externalTaskRetryAspect
                .handleErrorAfterThrown(this.joinPoint, instantIncidentException,
                                            this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage("RuntimeException: null");
        this.assertErrorDetails(instantIncidentException.getCause());
        this.assertNextRetryInterval(0);
        this.assertNoRemainingRetries();
    }


    @Test
    public void instantIncidentException() {
        InstantIncidentException instantIncidentException = new InstantIncidentException("instant-incident-no-retries-plz");
        this.externalTaskRetryAspect
                .handleErrorAfterThrown(this.joinPoint,
                        instantIncidentException,
                        this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage("InstantIncidentException: instant-incident-no-retries-plz");
        this.assertErrorDetails(instantIncidentException);
        this.assertNextRetryInterval(0);
        this.assertNoRemainingRetries();
    }


    @Test
    public void emptyBpmBusinessException() {
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new ExternalTaskBusinessError(), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoFailure();
        this.verifyBpmnErrorWithoutVariables();

        // assert
        this.assertBpmnErrorCode(null);
        this.assertErrorMessage(null);
    }


    @Test
    public void onlyCodeBpmBusinessException() {
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new ExternalTaskBusinessError("only-code"), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoFailure();
        this.verifyBpmnErrorWithoutVariables();

        // assert
        this.assertBpmnErrorCode("only-code");
        this.assertErrorMessage(null);
    }


    @Test
    public void bpmBusinessException() {
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new ExternalTaskBusinessError("code", "bpmn-error-message"), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoFailure();
        this.verifyBpmnErrorWithoutVariables();

        // assert
        this.assertBpmnErrorCode("code");
        this.assertErrorMessage("bpmn-error-message");
    }

    @Test
    public void bpmBusinessExceptionWithVariables() {
        final VariableMap variables = Variables.createVariables().putValue("darth", "vader").putValue("r2d", 2);
        final ExternalTaskBusinessError businessError = new ExternalTaskBusinessError("var-code", "bpmn-error-with-variables", variables);
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, businessError, this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoFailure();
        this.verifyBpmnErrorWithVariables();

        // assert
        this.assertBpmnErrorCode("var-code");
        this.assertErrorMessage("bpmn-error-with-variables");
        assertEquals(2, this.errorVariables.getValue().size());
        assertEquals("vader", this.errorVariables.getValue().get("darth"));
        assertEquals(2, this.errorVariables.getValue().get("r2d"));
    }

}
