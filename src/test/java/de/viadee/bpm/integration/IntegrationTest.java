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
package de.viadee.bpm.integration;

import de.viadee.bpm.externaltask.retry.aspect.config.CamundaExternalTaskRetryAspectConfiguration;
import de.viadee.bpm.externaltask.retry.aspect.config.ExternalTaskRetryAspectConfiguration;
import de.viadee.bpm.externaltask.retry.aspect.config.OperatonExternalTaskRetryAspectConfiguration;
import de.viadee.bpm.externaltask.retry.aspect.service.FailureService;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = {ExternalTaskRetryAspectConfiguration.class,
        CamundaExternalTaskRetryAspectConfiguration.class,
        OperatonExternalTaskRetryAspectConfiguration.class,
        IntegrationTestCamundaWorker.class,
        IntegrationTestOperatonWorker.class})
public class IntegrationTest {


    @Autowired
    IntegrationTestCamundaWorker camundaExternalTaskHandler;
    @Autowired
    IntegrationTestOperatonWorker operatonExternalTaskHandler;

    @MockitoSpyBean
    FailureService failureService;




    @Test
    void catchCamundaError() {
        try {
            camundaExternalTaskHandler.execute(null, null);
        } catch (RuntimeException e) {

        }

        Mockito.verify(failureService, Mockito.timeout(10000)).handleFailure(any(), any(), any(), any());
    }

    @Test
    void catchOperatonError() {
        try {
            operatonExternalTaskHandler.execute(null, null);
        } catch (RuntimeException e) {

        }

        Mockito.verify(failureService, Mockito.timeout(10000)).handleFailure(any(), any(), any(), any());
    }

}
