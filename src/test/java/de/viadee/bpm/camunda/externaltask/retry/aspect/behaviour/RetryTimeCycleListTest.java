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

import java.util.Objects;

import static org.mockito.Mockito.when;


public class RetryTimeCycleListTest extends BaseTest {

    //                                              10.   9.   8.      7.   6.        5.  4.      3.      2.      1.
    private static final String RETRY_CYCLE_LIST = "PT10S,PT2M,PT3M45S,PT4H,PT5H42M2S,P4D,P5DT11H,P5DT11M,P5DT33S,P11DT11H11M11S";

    @Test
    public void retryTimeCycleListNullTest() {
        this.runTestAndReturnNextInterval(null);
        this.assertNextRetryInterval(10 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList11Test() {
        this.runTestAndReturnNextInterval(11);
        this.assertNextRetryInterval(10 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList10Test() {
        this.runTestAndReturnNextInterval(10);
        this.assertNextRetryInterval(2 * MINUTES_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList9Test() {
        this.runTestAndReturnNextInterval(9);
        this.assertNextRetryInterval(3 * MINUTES_TO_MILLIS + 45 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList8Test() {
        this.runTestAndReturnNextInterval(8);
        this.assertNextRetryInterval(4 * HOURS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList7Test() {
        this.runTestAndReturnNextInterval(7);
        this.assertNextRetryInterval(5 * HOURS_TO_MILLIS + 42 * MINUTES_TO_MILLIS + 2 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList6Test() {
        this.runTestAndReturnNextInterval(6);
        this.assertNextRetryInterval(4 * DAYS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList5Test() {
        this.runTestAndReturnNextInterval(5);
        this.assertNextRetryInterval(5 * DAYS_TO_MILLIS + 11 * HOURS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList4Test() {
        this.runTestAndReturnNextInterval(4);
        this.assertNextRetryInterval(5 * DAYS_TO_MILLIS + 11 * MINUTES_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList3Test() {
        this.runTestAndReturnNextInterval(3);
        this.assertNextRetryInterval(5 * DAYS_TO_MILLIS + 33 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList2Test() {
        this.runTestAndReturnNextInterval(2);
        this.assertNextRetryInterval(11 * DAYS_TO_MILLIS + 11 * HOURS_TO_MILLIS + 11 * MINUTES_TO_MILLIS + 11 * SECONDS_TO_MILLIS);
    }

    @Test
    public void retryTimeCycleList1Test() {
        this.runTestAndReturnNextInterval(1);
        this.assertNextRetryInterval(0);
    }


    public void runTestAndReturnNextInterval(final Integer retries) {
        // prepare
        when(this.externalTask.getRetries()).thenReturn(retries); // 1. retry
        when(this.externalTask.getExtensionProperty(this.properties.getRetryTimeCycleIdentifier())).thenReturn(RETRY_CYCLE_LIST);

        // test
        this.externalTaskRetryAspect.handleErrorAfterThrown(this.joinPoint, new RuntimeException(), this.externalTask, this.externalTaskService);

        // verify
        this.verifyNoBpmnErrorAtAll();
        this.verifyHandleFailure();

        // assert
        if (Objects.isNull(retries)) {
            this.assertRemainingRetries(10);
        } else {
            this.assertRemainingRetries(retries - 1);
        }
    }

}
