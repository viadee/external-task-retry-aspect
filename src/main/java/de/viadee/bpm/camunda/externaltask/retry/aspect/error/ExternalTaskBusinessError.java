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
package de.viadee.bpm.camunda.externaltask.retry.aspect.error;

import org.camunda.bpm.client.task.ExternalTaskService;

import java.util.Map;
import java.util.Objects;

public final class ExternalTaskBusinessError extends RuntimeException {

    private String errorCode;
    private String errorMessage;
    private Map<String, Object> variables;

    /**
     * This error-type is used to report a business error in the context of the current task.
     * For more information see @{@link ExternalTaskService}
     *
     */
    public ExternalTaskBusinessError() {
        // nop
    }


    /**
     * This error-type is used to report a business error in the context of the current task.
     * For more information see @{@link ExternalTaskService}
     *
     * @param errorCode the error-code is used to identify the BPMN error in process
     */
    public ExternalTaskBusinessError(final String errorCode) {
        this(errorCode, null);
    }


    /**
     * This error-type is used to report a business error in the context of the current task.
     * For more information see @{@link ExternalTaskService}
     *
     * @param errorCode the error-code is used to identify the BPMN error in process
     * @param errorMessage reason for failure
     */
    public ExternalTaskBusinessError(final String errorCode, final String errorMessage) {
        this(errorCode, errorMessage, null);
    }


    /**
     * This error-type is used to report a business error in the context of the current task.
     * For more information see @{@link ExternalTaskService}
     *
     * @param errorCode used to identify the BPMN error in process
     * @param errorMessage reason for failure which is passed to the process
     * @param variables passed to the process when the error is caught
     */
    public ExternalTaskBusinessError(final String errorCode, final String errorMessage, final Map<String, Object> variables) {
        super(Objects.nonNull(errorMessage) ? errorMessage : errorCode);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.variables = variables;
    }


    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Map<String, Object> getVariables() {
        return this.variables;
    }
}
