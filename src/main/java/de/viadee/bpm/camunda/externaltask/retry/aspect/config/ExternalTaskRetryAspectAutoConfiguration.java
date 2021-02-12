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
package de.viadee.bpm.camunda.externaltask.retry.aspect.config;

import de.viadee.bpm.camunda.externaltask.retry.aspect.ExternalTaskRetryAspect;
import de.viadee.bpm.camunda.externaltask.retry.aspect.service.BusinessErrorService;
import de.viadee.bpm.camunda.externaltask.retry.aspect.service.FailureService;
import de.viadee.bpm.camunda.externaltask.retry.aspect.service.PropertyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(ExternalTaskRetryAspectProperties.class)
public class ExternalTaskRetryAspectAutoConfiguration {

    private final ExternalTaskRetryAspectProperties externalTaskRetryAspectProperties;

    public ExternalTaskRetryAspectAutoConfiguration(final ExternalTaskRetryAspectProperties externalTaskRetryAspectProperties) {
        this.externalTaskRetryAspectProperties = externalTaskRetryAspectProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public BusinessErrorService businessErrorService() {
        return new BusinessErrorService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FailureService failureHandlingService() {
        return new FailureService(this.propertyService());
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalTaskRetryAspect externalTaskRetryAspect() {
        return new ExternalTaskRetryAspect(
                this.businessErrorService(),
                this.failureHandlingService()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public PropertyService propertyService() {
        return new PropertyService(
                this.externalTaskRetryAspectProperties.getDefaultRetryTimeCycle(),
                this.externalTaskRetryAspectProperties.getRetryTimeCycleIdentifier());
    }
}