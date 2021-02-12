package de.viadee.bpm.camunda.externaltask.retry.aspect.model;

import java.util.Objects;
import java.util.regex.Pattern;

public final class RetryValueVault {

    //@formatter:off
    private static final long    FALLBACK_INTERVAL     = 5 * 60 * 1000L; // 5 minutes
    private static final String  FALLBACK_RETRY_CYCLE  = "R3/PT5M";

    private static final String  RETRY_LIST_REGEX      = "^([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)(,([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?))*$";
    private static final String  RETRY_CYCLE_REGEX     = "^(?:[Rr](?<times>\\d+)/)(?<interval>(?:[Pp](?:\\d+[Yy])?)(?:\\d+[Mm])?(?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)$";

    private static final Pattern RETRY_LIST_PATTERN    = Pattern.compile(RETRY_LIST_REGEX);
    private static final Pattern RETRY_CYCLE_PATTERN   = Pattern.compile(RETRY_CYCLE_REGEX);
    //@formatter:on

    private final String defaultRetryConfig;
    private final String retryConfigIdentifier;


    public RetryValueVault(final String defaultRetryConfig, final String retryConfigIdentifier) {
        this.retryConfigIdentifier = retryConfigIdentifier;

        if (Objects.isNull(defaultRetryConfig) || defaultRetryConfig.trim().isEmpty()) {
            this.defaultRetryConfig = FALLBACK_RETRY_CYCLE;
        } else {
            this.defaultRetryConfig = defaultRetryConfig.replace(" ","").toUpperCase();
        }
    }

    public String getRetryConfigIdentifier() {
        return this.retryConfigIdentifier;
    }

    public String getDefaultRetryConfig() {
        return this.defaultRetryConfig;
    }

    public Pattern getRetryListPattern() {
        return RETRY_LIST_PATTERN;
    }

    public Pattern getTimeCyclePattern() {
        return RETRY_CYCLE_PATTERN;
    }

    public String getFallbackRetryTimeCycle() {
        return FALLBACK_RETRY_CYCLE;
    }

    public long getFallbackInterval() {
        return FALLBACK_INTERVAL;
    }
}
