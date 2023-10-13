/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2021, viadee IT-Unternehmensberatung AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.BaseTest;
import de.viadee.bpm.camunda.externaltask.retry.aspect.error.ExternalTaskBusinessError;
import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

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
        this.assertErrorMessage("RuntimeException: root-cause (InstantIncident: no-retries)");
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
        this.assertErrorMessage("InstantIncident");
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
        this.assertErrorMessage("RuntimeException: null (InstantIncident)");
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
        this.assertErrorMessage("InstantIncident: instant-incident-no-retries-plz");
        this.assertErrorDetails(instantIncidentException);
        this.assertNextRetryInterval(0);
        this.assertNoRemainingRetries();
    }


    @Test
    public void instantCustomIncidentException() {
        RuntimeException rootCause = new RuntimeException("root-cause");
        CustomTestInstantErrorType customError = new CustomTestInstantErrorType("instant-custom-incident", rootCause);
        this.externalTaskRetryAspect
                .handleErrorAfterThrown(this.joinPoint, customError,
                        this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        this.assertErrorMessage("RuntimeException: root-cause (InstantIncident: instant-custom-incident)");
        this.assertErrorDetails(rootCause);
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

    @Test
    public void bpmCustomBusinessException() {
        final CustomTestBusinessErrorType customBusinessError = new CustomTestBusinessErrorType("custom-code");
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, customBusinessError, this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoFailure();
        this.verifyBpmnErrorWithoutVariables();

        // assert
        this.assertBpmnErrorCode("custom-code");
    }

}
