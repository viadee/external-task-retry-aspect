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

import de.viadee.bpm.camunda.externaltask.retry.aspect.config.ExternalTaskRetryAspectProperties;

import java.util.regex.Pattern;

public final class RetryConfigValues {

    //@formatter:off
    private static final long    FALLBACK_INTERVAL           = 5 * 60 * 1000L; // 5 minutes
    private static final String  FALLBACK_RETRY_CONFIG       = "R3/PT5M";
    private static final String  FALLBACK_RETRY_CONFIG_NAME  = "RETRY_CONFIG";

    private static final String  RETRY_LIST_REGEX            = "^([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)(,([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?))*$";
    private static final String  RETRY_CYCLE_REGEX           = "^(?:[Rr](?<times>\\d+)/)(?<interval>(?:[Pp](?:\\d+[Yy])?)(?:\\d+[Mm])?(?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)$";

    private static final Pattern RETRY_LIST_PATTERN          = Pattern.compile(RETRY_LIST_REGEX);
    private static final Pattern RETRY_CYCLE_PATTERN         = Pattern.compile(RETRY_CYCLE_REGEX);
    //@formatter:on

    private final ExternalTaskRetryAspectProperties properties;


    public RetryConfigValues(final ExternalTaskRetryAspectProperties properties) {
        this.properties = properties;
    }

    public String getRetryConfigName() {
        return this.properties.getIdentifier();
    }

    public String getDefaultRetryConfig() {
        return this.properties.getDefaultBehavior();
    }

    public Pattern getRetryListPattern() {
        return RETRY_LIST_PATTERN;
    }

    public Pattern getTimeCyclePattern() {
        return RETRY_CYCLE_PATTERN;
    }

    public String getFallbackRetryTimeCycle() {
        return FALLBACK_RETRY_CONFIG;
    }

    public long getFallbackInterval() {
        return FALLBACK_INTERVAL;
    }
}
