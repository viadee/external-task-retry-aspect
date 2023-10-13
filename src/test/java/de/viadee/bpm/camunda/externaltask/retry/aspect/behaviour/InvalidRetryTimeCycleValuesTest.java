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
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.when;


@TestPropertySource(properties = "de.viadee.bpm.camunda.external-task.retry-config.default-behavior=R3/PT37M")
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
        when(this.externalTask.getExtensionProperty(this.properties.getIdentifier())).thenReturn(retryTimeCycle);

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
