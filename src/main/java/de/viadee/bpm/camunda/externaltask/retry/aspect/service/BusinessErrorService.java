package de.viadee.bpm.camunda.externaltask.retry.aspect.service;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.ExternalTaskBusinessError;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.LoggerFactory;

import java.util.Objects;


public class BusinessErrorService {


    public void handleError(final Class<?> origin,
                            final ExternalTask externalTask,
                            final ExternalTaskService externalTaskService,
                            final ExternalTaskBusinessError externalTaskBusinessError) {

        this.logBusinessError(origin, externalTaskBusinessError);

        if (Objects.isNull(externalTaskBusinessError.getVariables())) {
            // don't send variables, if null
            externalTaskService.handleBpmnError(externalTask,
                                                    externalTaskBusinessError.getErrorCode(),
                                                    externalTaskBusinessError.getErrorMessage());

        } else {
            externalTaskService.handleBpmnError(externalTask,
                                                    externalTaskBusinessError.getErrorCode(),
                                                    externalTaskBusinessError.getErrorMessage(),
                                                    externalTaskBusinessError.getVariables());
        }
    }


    private void logBusinessError(final Class<?> origin, final ExternalTaskBusinessError externalTaskBusinessError) {
        final String errorLogString = String.format("BusinessError, Code: %s, Message: %s",
                                                        externalTaskBusinessError.getErrorCode(),
                                                        externalTaskBusinessError.getErrorMessage());

        LoggerFactory.getLogger(origin).error(errorLogString);
    }
}
