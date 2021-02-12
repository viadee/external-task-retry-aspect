package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import java.util.Objects;

public class RetryCount {

    private final Integer retries;

    public RetryCount(final Integer retries) {
        this.retries = retries;
    }

    public boolean hasRetries() {
        return Objects.nonNull(this.retries);
    }

    public Integer nextRetries() {
        if (this.getRetries() <= 0) return 0;
        else return this.getRetries() - 1;
    }

    public Integer getRetries() {
        return this.retries;
    }

}
