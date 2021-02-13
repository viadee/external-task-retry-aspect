/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2021, viadee IT-Unternehmensberatung AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
                                       final Exception exception,
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
