package de.viadee.bpm.camunda.externaltask.retry.aspect;

import de.viadee.bpm.camunda.externaltask.retry.aspect.error.ExternalTaskBusinessError;
import de.viadee.bpm.camunda.externaltask.retry.aspect.error.InstantIncidentException;
import de.viadee.bpm.camunda.externaltask.retry.aspect.service.BusinessErrorService;
import de.viadee.bpm.camunda.externaltask.retry.aspect.service.FailureService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.ExternalTaskServiceImpl;


@Aspect
public class ExternalTaskRetryAspect {

    private final BusinessErrorService businessErrorService;
    private final FailureService failureService;

    public ExternalTaskRetryAspect(final BusinessErrorService businessErrorService, final FailureService failureService) {
        this.businessErrorService = businessErrorService;
        this.failureService = failureService;
    }

    @Pointcut(value = "execution(public void  org.camunda.bpm.client.task.ExternalTaskHandler.execute(..)) " +
                                    "&& args(externalTask, externalTaskService)",
              argNames = "externalTask,externalTaskService")
    public void externalTaskHandlerExecute(final ExternalTask externalTask,
                                           final ExternalTaskServiceImpl externalTaskService) {
    }

    @AfterThrowing(pointcut = "externalTaskHandlerExecute(externalTask, externalTaskService)",
                   throwing = "exception", argNames = "joinPoint,exception,externalTask,externalTaskService")
    public void handleErrorAfterThrown(final JoinPoint joinPoint,
                                       final Throwable exception,
                                       final ExternalTask externalTask,
                                       final ExternalTaskService externalTaskService) {

        if (exception instanceof ExternalTaskBusinessError) {
            this.businessErrorService.handleError(joinPoint.getTarget().getClass(), externalTask, externalTaskService, (ExternalTaskBusinessError) exception);

        } else if (exception instanceof InstantIncidentException) {
            this.failureService.handleFailure(joinPoint.getTarget().getClass(), externalTask, externalTaskService, exception, true);

        } else {
            this.failureService.handleFailure(joinPoint.getTarget().getClass(), externalTask, externalTaskService, exception);

        }
    }
}
