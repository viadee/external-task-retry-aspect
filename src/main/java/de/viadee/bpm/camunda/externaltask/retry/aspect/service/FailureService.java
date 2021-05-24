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
package de.viadee.bpm.camunda.externaltask.retry.aspect.service;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.isNull;


public class FailureService {

    private final PropertyService propertyService;

    public FailureService(final PropertyService propertyService) {
        this.propertyService = propertyService;
    }


    public void handleFailure(final Class<?> origin,
                              final ExternalTask externalTask,
                              final ExternalTaskService externalTaskService,
                              final Exception exception) {

        this.handleFailure(origin, externalTask, externalTaskService, exception, false);
    }


    public void handleFailure(final Class<?> origin,
                              final ExternalTask externalTask,
                              final ExternalTaskService externalTaskService,
                              final Exception exception,
                              final boolean directIncident) {

        final int remainingRetries = directIncident ? 0 : this.propertyService.remainingRetries(externalTask);
        final long nextRetryInterval = directIncident ? 0 : this.propertyService.nextRetryInterval(externalTask);

        this.logFailure(origin, exception, remainingRetries, nextRetryInterval);

        externalTaskService.handleFailure(
                externalTask,
                this.getErrorMessage(exception),
                this.getStackTrace(exception),
                remainingRetries,
                nextRetryInterval);
    }


    private String getErrorMessage(final Throwable exception) {

        if (!(exception instanceof InstantIncidentException)) {
            // AnotherTypeException() -> "AnotherType: another-message"
            return exception.getClass().getSimpleName() + ": " + exception.getMessage();
        }

        if (isNull(exception.getMessage())) { // no message

            if (isNull(exception.getCause())) {
                // no root-cause: InstantIncidentException()
                // -> InstantIncident
                return "InstantIncident";

            } else {
                // with root-cause: InstantIncidentException(cause)
                // -> CauseType: cause-message (InstantIncident)
                return format("%s: %s (InstantIncident)",
                        exception.getCause().getClass().getSimpleName(),
                        exception.getCause().getMessage());
            }

        } else { // with message

            if (isNull(exception.getCause())) {
                // no root-cause: InstantIncidentException("message")
                // -> InstantIncident: message
                return "InstantIncident: " + exception.getMessage();

            } else {
                // with root-cause InstantIncidentException("message", cause)
                // -> CauseType: cause-message (InstantIncident: message)
                return format("%s: %s (InstantIncident: %s)",
                        exception.getCause().getClass().getSimpleName(),
                        exception.getCause().getMessage(),
                        exception.getMessage());
            }
        }
    }


    private String getStackTrace(final Throwable throwable) {
        if (throwable instanceof InstantIncidentException && Objects.nonNull(throwable.getCause())) {
            // if instant-incident, probably root cause is more relevant
            return this.getStackTrace(throwable.getCause());
        }
        StringWriter stackTrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTrace, true);
        throwable.printStackTrace(printWriter);
        return stackTrace.getBuffer().toString();
    }


    private void logFailure(final Class<?> origin, final Throwable throwable, final int remainingRetries, final Long nextRetryInterval) {
        // log remaining time only if retries > 0
        final String millisIfRemainingRetry = format("%s", (remainingRetries == 0) ? "" : ", next retry in " + nextRetryInterval + "ms");

        final String errorLogString =
                format("%s: %s. There are %s retry(s) left%s",
                        throwable.getClass().getSimpleName(),
                        throwable.getMessage(),
                        remainingRetries,
                        millisIfRemainingRetry);

        LoggerFactory.getLogger(origin).error(errorLogString, throwable);
    }

}
