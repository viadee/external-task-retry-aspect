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