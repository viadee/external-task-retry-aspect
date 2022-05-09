package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.ExternalTaskBusinessError;

public class CustomTestBusinessErrorType extends ExternalTaskBusinessError {
    public CustomTestBusinessErrorType(final String message) {
        super(message);
    }
}
