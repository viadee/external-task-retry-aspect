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
package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import com.sun.istack.internal.NotNull;
import org.camunda.bpm.client.task.ExternalTask;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;

public class RetryBehaviour {

    private final RetryCount retryCount;
    private final RetryConfig retryConfig;
    private final RetryConfigValues valueVault;

    public RetryBehaviour(final ExternalTask externalTask, final RetryConfigValues valueVault) {
        this.valueVault = valueVault;
        this.retryCount = new RetryCount(externalTask.getRetries());
        this.retryConfig = new RetryConfig(externalTask.getExtensionProperty(valueVault.getRetryConfigName()), valueVault);
    }

    public boolean hasRetries() {
        return this.retryCount.hasRetries();
    }

    public Integer nextRetries() {
        return this.retryCount.nextRetries();
    }

    public int determineRetriesFromConfig() {
        return this.readNumberOfRetriesFromProperty(this.retryConfig.getRetryProperty(), true);
    }

    private int readNumberOfRetriesFromProperty(String retryTimeCycle, final boolean tryDefault) {
        Matcher retryListMatcher = this.valueVault.getRetryListPattern().matcher(retryTimeCycle);
        Matcher retryCycleMatcher = this.valueVault.getTimeCyclePattern().matcher(retryTimeCycle);

        if (retryListMatcher.matches()) {
            // eg. "PT10M" = 1 retries
            // eg. "PT10M,PT1H" = 2 retries
            return retryTimeCycle.split(",").length;

        } else if (retryCycleMatcher.matches()) {
            // eg. "R5/PT10M"
            String times = retryCycleMatcher.group("times");
            return Integer.parseInt(times);

        } else if (tryDefault) {
            return this.readNumberOfRetriesFromProperty(this.valueVault.getDefaultRetryConfig(), false);

        } else {
            return this.readNumberOfRetriesFromProperty(this.valueVault.getFallbackRetryTimeCycle(), false);
        }
    }

    public long nextRetryInterval(final Integer remainingRetries) {
        return this.nextRetryInterval(remainingRetries, this.retryConfig.getRetryProperty(), true);
    }

    private long nextRetryInterval(final Integer remainingRetries, @NotNull String retryProperty, final boolean tryDefault) {
        if (remainingRetries == null || remainingRetries == 0) {
            return 0L;
        }

        retryProperty = retryProperty.replace(" ", "").toUpperCase();

        Matcher retryListMatcher = this.valueVault.getRetryListPattern().matcher(retryProperty);
        Matcher retryCycleMatcher = this.valueVault.getTimeCyclePattern().matcher(retryProperty);

        if (retryListMatcher.matches()) {
            int length = retryProperty.split(",").length;

            //    eg: PT1M,PT2M,PT1H,PT2H,P3D = 5 retries (length = 5)
            //          1.   2.   3.   4.  5.   retry
            //   eg: 4 remaining-retries -> next retry: PT2M (2.)
            // calc: 5 retries in total (length) - 4 remaining = 1 (= 2nd array-position)
            if (remainingRetries >= length) {
                return intervalToMilliSeconds(retryProperty.split(",")[0]);

            } else {
                return intervalToMilliSeconds(retryProperty.split(",")[length - remainingRetries]);
            }

        } else if (retryCycleMatcher.matches()) {
            // eg: PT1H, R1/PT2H, R2/PT5M, R3/P1D
            return intervalToMilliSeconds(retryCycleMatcher.group("interval"));

        } else if (tryDefault) {
            // no list, no match -> default
            return nextRetryInterval(remainingRetries, this.valueVault.getDefaultRetryConfig(), false);

        } else {
            return this.nextRetryInterval(remainingRetries, this.valueVault.getFallbackRetryTimeCycle(), false);
        }
    }

    private long intervalToMilliSeconds(final String interval) {
        try {
            return Duration.parse(interval).getSeconds() * 1000L;

        } catch (final DateTimeParseException exception) {
            return this.valueVault.getFallbackInterval(); // better fallback than exception

        }
    }

}
