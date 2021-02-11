package de.viadee.bpm.camunda.externaltask.retry.aspect.service;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;


public class FailureService {

    private final PropertyService propertyService;

    public FailureService(final PropertyService propertyService) {
        this.propertyService = propertyService;
    }


    public void handleFailure(final Class<?> origin,
                              final ExternalTask externalTask,
                              final ExternalTaskService externalTaskService,
                              final Throwable throwable) {

        this.handleFailure(origin, externalTask, externalTaskService, throwable, false);
    }


    public void handleFailure(final Class<?> origin,
                              final ExternalTask externalTask,
                              final ExternalTaskService externalTaskService,
                              final Throwable throwable,
                              final boolean directIncident) {

        final int remainingRetries = directIncident ? 0 : this.propertyService.determineRemainingRetries(externalTask);
        final long nextRetryInterval = directIncident ? 0 : this.propertyService.nextRetryInterval(externalTask);

        this.logFailure(origin, throwable, remainingRetries, nextRetryInterval);

        externalTaskService.handleFailure(
                externalTask,
                this.getErrorMessage(throwable),
                this.getStackTrace(throwable),
                remainingRetries,
                nextRetryInterval);
    }


    private String getErrorMessage(final Throwable throwable) {
        if (throwable instanceof InstantIncidentException && Objects.nonNull(throwable.getCause())) {
            // if instant-incident, probably root cause is more relevant
            return this.getErrorMessage(throwable.getCause());
        }
        return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
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
        final String millisIfRemainingRetry = String.format("%s", (remainingRetries == 0) ? "" : ", next retry in " + nextRetryInterval + "ms");

        final String errorLogString =
                String.format("%s: %s. There are %s retry(s) left%s",
                        throwable.getClass().getSimpleName(),
                        throwable.getMessage(),
                        remainingRetries,
                        millisIfRemainingRetry);

        LoggerFactory.getLogger(origin).error(errorLogString, throwable);
    }

}
