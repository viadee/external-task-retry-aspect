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
package de.viadee.bpm.externaltask.retry.aspect.model;


import org.camunda.bpm.client.task.ExternalTask;

import java.util.Map;

public class ExternalTaskServiceAdapter {

    private final org.camunda.bpm.client.task.ExternalTaskService camundaExternalTask;
    private final org.operaton.bpm.client.task.ExternalTaskService operatonExternalTask;

    public ExternalTaskServiceAdapter(org.camunda.bpm.client.task.ExternalTaskService camundaExternalTask) {
        this.camundaExternalTask = camundaExternalTask;
        this.operatonExternalTask = null;
    }

    public ExternalTaskServiceAdapter(org.operaton.bpm.client.task.ExternalTaskService operatonExternalTask) {
        this.camundaExternalTask = null;
        this.operatonExternalTask = operatonExternalTask;
    }


    public void handleBpmnError(ExternalTaskAdapter externalTask, String errorMessage, String errorCode) {
        if (camundaExternalTask != null && externalTask.getCamundaExternalTask() != null) {
            camundaExternalTask.handleBpmnError(externalTask.getCamundaExternalTask(), errorMessage, errorCode);
        } else if (operatonExternalTask != null && externalTask.getOperatonExternalTask() != null) {
            operatonExternalTask.handleBpmnError(externalTask.getOperatonExternalTask(), errorMessage, errorCode);
        } else {
            throw new IllegalArgumentException("ExternalTask Adapter had different type than ExternalTaskServiceAdapter!");
        }
    }

    public void handleBpmnError(ExternalTaskAdapter externalTask, String errorMessage, String errorCode, Map<String,Object> variables) {
        if (camundaExternalTask != null && externalTask.getCamundaExternalTask() != null) {
            camundaExternalTask.handleBpmnError(externalTask.getCamundaExternalTask(), errorMessage, errorCode, variables);
        } else if (operatonExternalTask != null && externalTask.getOperatonExternalTask() != null) {
            operatonExternalTask.handleBpmnError(externalTask.getOperatonExternalTask(), errorMessage, errorCode, variables);
        } else {
            throw new IllegalArgumentException("ExternalTask Adapter had different type than ExternalTaskServiceAdapter!");
        }
    }

    public void handleFailure(ExternalTaskAdapter externalTask, String errorMessage, String stackTrace, int remainigRetries, long nextRetryInterval) {
        if (camundaExternalTask != null && externalTask.getCamundaExternalTask() != null) {
            camundaExternalTask.handleFailure(externalTask.getCamundaExternalTask(), errorMessage, stackTrace, remainigRetries, nextRetryInterval);
        } else if (operatonExternalTask != null && externalTask.getOperatonExternalTask() != null) {
            operatonExternalTask.handleFailure(externalTask.getOperatonExternalTask(), errorMessage, stackTrace, remainigRetries, nextRetryInterval);
        } else {
            throw new IllegalArgumentException("ExternalTask Adapter had different type than ExternalTaskServiceAdapter!");
        }
    }

}
