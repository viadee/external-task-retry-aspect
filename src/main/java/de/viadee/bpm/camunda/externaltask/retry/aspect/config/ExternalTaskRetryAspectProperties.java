package de.viadee.bpm.camunda.externaltask.retry.aspect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "de.viadee.bpm.camunda.external-task")
public class ExternalTaskRetryAspectProperties {

    //@formatter:off
    private String defaultRetryTimeCycle     = "R3/PT5M";
    private String retryTimeCycleIdentifier  = "RETRY_CONFIG";
    //@formatter:on


    public String getDefaultRetryTimeCycle() {
        return this.defaultRetryTimeCycle;
    }

    public void setDefaultRetryTimeCycle(final String defaultRetryTimeCycle) {
        this.defaultRetryTimeCycle = defaultRetryTimeCycle;
    }

    public String getRetryTimeCycleIdentifier() {
        return this.retryTimeCycleIdentifier;
    }

    public void setRetryTimeCycleIdentifier(final String retryTimeCycleIdentifier) {
        this.retryTimeCycleIdentifier = retryTimeCycleIdentifier;
    }

}
