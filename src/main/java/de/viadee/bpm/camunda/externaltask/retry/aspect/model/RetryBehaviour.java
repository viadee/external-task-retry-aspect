package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import org.camunda.bpm.client.task.ExternalTask;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;

public class RetryBehaviour {

    private final RetryCount retryCount;
    private final RetryConfig retryConfig;
    private final RetryValueVault valueVault;

    public RetryBehaviour(final ExternalTask externalTask, final RetryValueVault valueVault) {
        this.valueVault = valueVault;
        this.retryCount = new RetryCount(externalTask.getRetries());
        this.retryConfig = new RetryConfig(externalTask.getExtensionProperty(valueVault.getRetryConfigIdentifier()), valueVault);
    }

    public boolean hasRetries() {
        return this.retryCount.hasRetries();
    }

    public Integer nextRetries() {
        return this.retryCount.nextRetries();
    }

    public int determineRetriesFromConfig() {
        return this.readNumberOfRetriesFromProperty(this.retryConfig.getRetryProperty(), true);
    }

    private int readNumberOfRetriesFromProperty(String retryTimeCycle, final boolean tryDefault) {
        Matcher retryListMatcher = this.valueVault.getRetryListPattern().matcher(retryTimeCycle);
        Matcher retryCycleMatcher = this.valueVault.getTimeCyclePattern().matcher(retryTimeCycle);

        if (retryListMatcher.matches()) {
            // eg. "PT10M" = 1 retries
            // eg. "PT10M,PT1H" = 2 retries
            return retryTimeCycle.split(",").length;

        } else if (retryCycleMatcher.matches()) {
            // eg. "R5/PT10M"
            String times = retryCycleMatcher.group("times");
            return Integer.parseInt(times);

        } else if (tryDefault) {
            return this.readNumberOfRetriesFromProperty(this.valueVault.getDefaultRetryConfig(), false);

        } else {
            return this.readNumberOfRetriesFromProperty(this.valueVault.getFallbackRetryTimeCycle(), false);
        }
    }

    public long nextRetryInterval(final Integer remainingRetries) {
        return this.nextRetryInterval(remainingRetries, this.retryConfig.getRetryProperty(), true);
    }

    private long nextRetryInterval(final Integer remainingRetries, String retryProperty, final boolean tryDefault) {
        if (remainingRetries == null || remainingRetries == 0) {
            return 0L;
        }

        if (retryProperty == null || retryProperty.trim().isEmpty()) {
            // if empty retry-config, start again using the default
            return nextRetryInterval(remainingRetries, this.valueVault.getDefaultRetryConfig(), true);
        }

        retryProperty = retryProperty.replace(" ", "").toUpperCase();

        Matcher retryListMatcher = this.valueVault.getRetryListPattern().matcher(retryProperty);
        Matcher retryCycleMatcher = this.valueVault.getTimeCyclePattern().matcher(retryProperty);

        if (retryListMatcher.matches()) {
            int length = retryProperty.split(",").length;

            //    eg: PT1M,PT2M,PT1H,PT2H,P3D = 5 retries (length = 5)
            //          1.   2.   3.   4.  5.   retry
            //   eg: 4 remaining-retries -> next retry: PT2M (2.)
            // calc: 5 retries in total (length) - 4 remaining = 1 (= 2nd array-position)
            if (remainingRetries >= length) {
                return intervalToMilliSeconds(retryProperty.split(",")[0]);

            } else {
                return intervalToMilliSeconds(retryProperty.split(",")[length - remainingRetries]);
            }

        } else if (retryCycleMatcher.matches()) {
            // eg: PT1H, R1/PT2H, R2/PT5M, R3/P1D
            return intervalToMilliSeconds(retryCycleMatcher.group("interval"));

        } else if (tryDefault) {
            // no list, no match -> default
            return nextRetryInterval(remainingRetries, this.valueVault.getDefaultRetryConfig(), false);

        } else {
            return this.nextRetryInterval(remainingRetries, this.valueVault.getFallbackRetryTimeCycle(), false);
        }
    }

    private long intervalToMilliSeconds(final String interval) {
        try {
            return Duration.parse(interval).getSeconds() * 1000L;

        } catch (final DateTimeParseException exception) {
            return this.valueVault.getFallbackInterval(); // better fallback than exception

        }
    }

}
