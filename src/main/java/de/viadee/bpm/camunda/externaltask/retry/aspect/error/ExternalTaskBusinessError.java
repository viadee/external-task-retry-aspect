package de.viadee.bpm.camunda.externaltask.retry.aspect.error;

import java.util.Map;
import java.util.Objects;

public final class ExternalTaskBusinessError extends RuntimeException {

    private String errorCode;
    private String errorMessage;
    private Map<String, Object> variables;

    public ExternalTaskBusinessError() {
        // nop
    }

    public ExternalTaskBusinessError(final String errorCode) {
        this(errorCode, null);
    }

    public ExternalTaskBusinessError(final String errorCode, final String errorMessage) {
        this(errorCode, errorMessage, null);
    }

    public ExternalTaskBusinessError(final String errorCode, final String errorMessage, final Map<String, Object> variables) {
        super(Objects.nonNull(errorMessage) ? errorMessage : errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.variables = variables;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }
}
