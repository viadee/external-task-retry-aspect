package de.viadee.bpm.camunda.externaltask.retry.aspect;

import de.viadee.bpm.camunda.externaltask.retry.aspect.config.ExternalTaskRetryAspectAutoConfiguration;
import de.viadee.bpm.camunda.externaltask.retry.aspect.config.ExternalTaskRetryAspectProperties;
import org.aspectj.lang.JoinPoint;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

@SuppressWarnings({"unchecked", "rawtypes"})
@ContextConfiguration(classes = {ExternalTaskRetryAspectAutoConfiguration.class})
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public abstract class BaseTest {

    //@formatter:off
    static final long SECONDS_TO_MILLIS =                1000L; //             ms
    static final long MINUTES_TO_MILLIS =           60 * 1000L; //         s * ms
    static final long HOURS_TO_MILLIS   =      60 * 60 * 1000L; //     m * s * ms
    static final long DAYS_TO_MILLIS    = 24 * 60 * 60 * 1000L; // h * m * s * ms
    //@formatter:on

    final ExternalTask externalTask = mock(ExternalTask.class);
    final ExternalTaskService externalTaskService = mock(ExternalTaskService.class);
    final JoinPoint joinPoint = mock(JoinPoint.class);
    ArgumentCaptor<String> errorMessage;
    ArgumentCaptor<String> errorDetails;
    ArgumentCaptor<String> errorCode;
    ArgumentCaptor<Map> errorVariables;
    ArgumentCaptor<Integer> remainingRetries;
    ArgumentCaptor<Long> nextRetryInterval;

    @Autowired
    ExternalTaskRetryAspect externalTaskRetryAspect;

    @Autowired
    ExternalTaskRetryAspectProperties properties;


    @Before
    public void initTestData() {
        Mockito.reset(this.externalTask, this.externalTaskService, this.joinPoint);
        Mockito.when(this.joinPoint.getTarget()).thenReturn(Object.class);

        this.errorMessage = ArgumentCaptor.forClass(String.class);
        this.errorDetails = ArgumentCaptor.forClass(String.class);
        this.errorCode = ArgumentCaptor.forClass(String.class);
        this.errorVariables = ArgumentCaptor.forClass(Map.class);
        this.remainingRetries = ArgumentCaptor.forClass(Integer.class);
        this.nextRetryInterval = ArgumentCaptor.forClass(Long.class);
    }

    void verifyNoBpmnErrorAtAll() {
        Mockito.verify(this.externalTaskService,
                Mockito.times(0))
                    .handleBpmnError(any(ExternalTask.class), anyString(), anyString());

        Mockito.verify(this.externalTaskService,
                Mockito.times(0))
                    .handleBpmnError(any(ExternalTask.class), anyString(), anyString(), anyMap());
    }

    void verifyBpmnErrorWithoutVariables() {
        Mockito.verify(this.externalTaskService,
                Mockito.times(1))
                    .handleBpmnError(any(ExternalTask.class), this.errorCode.capture(), this.errorMessage.capture());

        Mockito.verify(this.externalTaskService,
                Mockito.times(0))
                    .handleBpmnError(any(ExternalTask.class), anyString(), anyString(), anyMap());
    }

    void verifyBpmnErrorWithVariables() {
        Mockito.verify(this.externalTaskService,
                Mockito.times(1))
                    .handleBpmnError(any(ExternalTask.class), this.errorCode.capture(), this.errorMessage.capture(), this.errorVariables.capture());

        Mockito.verify(this.externalTaskService,
                Mockito.times(0))
                    .handleBpmnError(any(ExternalTask.class), anyString(), anyString());
    }

    void verifyNoFailure() {
        Mockito.verify(this.externalTaskService,
                Mockito.times(0))
                    .handleFailure(any(ExternalTask.class), this.errorMessage.capture(), anyString(), anyInt(), anyLong());
    }

    void verifyHandleFailure() {
        Mockito.verify(this.externalTaskService,
                Mockito.times(1))
                    .handleFailure(any(ExternalTask.class), this.errorMessage.capture(), this.errorDetails.capture(), this.remainingRetries.capture(), this.nextRetryInterval.capture());
    }

    void assertErrorMessage(final String expectedErrorMessage) {
        assertEquals(expectedErrorMessage, this.errorMessage.getValue());
    }

    void assertErrorDetails(final Throwable expectedThrowable) {
        this.assertErrorDetails(this.getStackTrace(expectedThrowable));
    }

    void assertErrorDetails(final String expectedErrorDetails) {
        assertEquals(expectedErrorDetails, this.errorDetails.getValue());
    }

    void assertNoRemainingRetries() {
        this.assertRemainingRetries(0);
    }

    void assertRemainingRetries(final int expectedRemainingRetries) {
        assertEquals(expectedRemainingRetries, this.remainingRetries.getValue());
    }

    void assertNextRetryInterval(final long expectedNextRetryInterval) {
        assertEquals(expectedNextRetryInterval, this.nextRetryInterval.getValue());
    }

    void assertBpmnErrorCode(final String expectedBpmnErrorCode) {
        assertEquals(expectedBpmnErrorCode, this.errorCode.getValue());
    }

    String getStackTrace(final Throwable throwable) {
        StringWriter stackTrace = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTrace, true);
        throwable.printStackTrace(printWriter);
        return stackTrace.getBuffer().toString();
    }
}
