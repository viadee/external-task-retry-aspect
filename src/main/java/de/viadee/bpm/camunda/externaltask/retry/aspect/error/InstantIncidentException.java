package de.viadee.bpm.camunda.externaltask.retry.aspect.error;


public final class InstantIncidentException extends RuntimeException {

    public InstantIncidentException() {
    }

    public InstantIncidentException(final String message) {
        super(message);
    }

    public InstantIncidentException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InstantIncidentException(final Throwable cause) {
        super(cause);
    }

}
