package de.viadee.bpm.camunda.externaltask.retry.aspect.behaviour;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;

public class CustomTestInstantErrorType extends InstantIncidentException {
    public CustomTestInstantErrorType(final String message, final Throwable cause) {
        super(message, cause);
    }
}
