package de.viadee.bpm.camunda.externaltask.retry.aspect.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class PropertyService {
    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);

    private static final long FALLBACK_INTERVAL = 5 * 60 * 1000L; // 5 minutes
    private static final String FALLBACK_RETRY_TIME_CYCLE = "R3/PT5M";

    private final Pattern retryTimeListPattern = Pattern.compile(RETRY_TIME_LIST_PATTERN);
    private static final String RETRY_TIME_LIST_PATTERN = "^([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)(,([Pp](?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?))*$";

    private final Pattern retryTimeCyclePattern = Pattern.compile(RETRY_TIME_CYCLE_PATTERN);
    private static final String RETRY_TIME_CYCLE_PATTERN = "^(?:[Rr](?<times>\\d+)/)(?<interval>(?:[Pp](?:\\d+[Yy])?)(?:\\d+[Mm])?(?:\\d+[Dd])?(?:[Tt](?!$)(?:\\d+[Hh])?(?:\\d+[Mm])?(?:\\d+[Ss])?)?)$";

    private final String defaultRetryTimeCycle;
    private final String retryTimeCycleIdentifier;

    public PropertyService(final String defaultRetryTimeCycle,
                           final String retryTimeCycleIdentifier) {

        this.defaultRetryTimeCycle = defaultRetryTimeCycle;
        this.retryTimeCycleIdentifier = retryTimeCycleIdentifier;
    }


    public int determineRemainingRetries(final ExternalTask externalTask) {
        Integer retries = externalTask.getRetries();
        String retryTimeCycle = this.getRetryTimeCycleIdentifier(externalTask);
        log.trace("ExternalTask: id={}, retries={}, retryTimeCycle='{}'", externalTask.getId(), retries, retryTimeCycle);

        if (retries != null && retries > 0) {
            retries = retries - 1;

        } else //noinspection ConstantConditions
            if (retries != null && retries <= 0) {
            retries = 0;

        } else {
            retries = this.readNumberOfRetriesFromProperty(retryTimeCycle);
        }

        log.trace("{} remaining retry(s) for task (id={})", retries, externalTask.getId());
        return retries;
    }


    private int readNumberOfRetriesFromProperty(String retryTimeCycle) {
        return this.readNumberOfRetriesFromProperty(retryTimeCycle, true);
    }

    private int readNumberOfRetriesFromProperty(String retryTimeCycle, final boolean tryDefault) {
        if (retryTimeCycle == null || retryTimeCycle.trim().isEmpty()) {
            // if empty retry-config, start again using the default
            return readNumberOfRetriesFromProperty(this.defaultRetryTimeCycle);
        }

        retryTimeCycle = retryTimeCycle.replace(" ", "").toUpperCase();

        Matcher retryListMatcher = this.retryTimeListPattern.matcher(retryTimeCycle);
        Matcher retryCycleMatcher = this.retryTimeCyclePattern.matcher(retryTimeCycle);

        if (retryListMatcher.matches()) {
            // eg. "PT10M" = 1 retries
            // eg. "PT10M,PT1H" = 2 retries
            return retryTimeCycle.split(",").length;

        } else if (retryCycleMatcher.matches()) {
            // eg. "R5/PT10M"
            String times = retryCycleMatcher.group("times");
            return Integer.parseInt(times);

        } else if (tryDefault) {
            log.trace("no valid retry-time-cycle found in '{}', use default now (default={})", retryTimeCycle, this.defaultRetryTimeCycle);
            return this.readNumberOfRetriesFromProperty(this.defaultRetryTimeCycle, false);

        } else {
            log.trace("no valid retry-time-cycle found in '{}', use fallback now (fallback={})", retryTimeCycle, FALLBACK_RETRY_TIME_CYCLE);
            return this.readNumberOfRetriesFromProperty(FALLBACK_RETRY_TIME_CYCLE);
        }
    }


    private long nextRetryInterval(final Integer remainingRetries, String retryTimeCycle) {
        return this.nextRetryInterval(remainingRetries, retryTimeCycle, true);
    }


    private long nextRetryInterval(final Integer remainingRetries, String retryTimeCycle, final boolean tryDefault) {
        if (remainingRetries == null || remainingRetries == 0) {
            return 0L;
        }

        if (retryTimeCycle == null || retryTimeCycle.trim().isEmpty()) {
            // if empty retry-config, start again using the default
            return nextRetryInterval(remainingRetries, this.defaultRetryTimeCycle);
        }

        retryTimeCycle = retryTimeCycle.replace(" ", "").toUpperCase();

        Matcher retryListMatcher = this.retryTimeListPattern.matcher(retryTimeCycle);
        Matcher retryCycleMatcher = this.retryTimeCyclePattern.matcher(retryTimeCycle);

        if (retryListMatcher.matches()) {
            int length = retryTimeCycle.split(",").length;

            //    eg: PT1M,PT2M,PT1H,PT2H,P3D = 5 retries (length = 5)
            //          1.   2.   3.   4.  5.   retry
            //   eg: 4 remaining-retries -> next retry: PT2M (2.)
            // calc: 5 retries in total (length) - 4 remaining = 1 (= 2nd array-position)
            if (remainingRetries >= length) {
                return intervalToMilliSeconds(retryTimeCycle.split(",")[0]);

            } else {
                return intervalToMilliSeconds(retryTimeCycle.split(",")[length - remainingRetries]);
            }

        } else if (retryCycleMatcher.matches()) {
            // eg: PT1H, R1/PT2H, R2/PT5M, R3/P1D
            return intervalToMilliSeconds(retryCycleMatcher.group("interval"));

        } else if (tryDefault) {
            // no list, no match -> default
            return nextRetryInterval(remainingRetries, this.defaultRetryTimeCycle, false);

        } else {
            return this.nextRetryInterval(remainingRetries, FALLBACK_RETRY_TIME_CYCLE);
        }
    }


    public long nextRetryInterval(final ExternalTask externalTask) {
        final int remainingRetries = this.determineRemainingRetries(externalTask);
        final String retryConfig = this.getRetryTimeCycleIdentifier(externalTask);
        return nextRetryInterval(remainingRetries, retryConfig);
    }


    private String getRetryTimeCycleIdentifier(final ExternalTask externalTask) {
        return externalTask.getExtensionProperty(this.retryTimeCycleIdentifier);
    }


    private long intervalToMilliSeconds(final String interval) {
        try {
            return Duration.parse(interval).getSeconds() * 1000L;

        } catch (final DateTimeParseException exception) {
            return FALLBACK_INTERVAL; // better fallback than exception

        }
    }

}
