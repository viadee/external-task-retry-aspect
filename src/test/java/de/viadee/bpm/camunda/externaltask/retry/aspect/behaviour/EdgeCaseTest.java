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
