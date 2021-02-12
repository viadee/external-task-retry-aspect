package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import java.util.Objects;

public class RetryConfig {

    private final String retryProperty;

    public RetryConfig(final String retryProperty, final RetryValueVault valueVault) {
        if (Objects.isNull(retryProperty) || retryProperty.trim().isEmpty()) {
            this.retryProperty = valueVault.getDefaultRetryConfig();
        } else {
            this.retryProperty = retryProperty.replace(" ", "").toUpperCase();
        }
    }

    public String getRetryProperty() {
        return this.retryProperty;
    }

}
