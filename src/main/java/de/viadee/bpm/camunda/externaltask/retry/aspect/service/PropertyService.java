package de.viadee.bpm.camunda.externaltask.retry.aspect.service;

import de.viadee.bpm.camunda.externaltask.retry.aspect.model.RetryBehaviour;
import de.viadee.bpm.camunda.externaltask.retry.aspect.model.RetryValueVault;
import org.camunda.bpm.client.task.ExternalTask;


public final class PropertyService {

    private final RetryValueVault valueVault;

    public PropertyService(final String defaultRetryTimeCycle, final String retryTimeCycleIdentifier) {
        this.valueVault = new RetryValueVault(defaultRetryTimeCycle, retryTimeCycleIdentifier);
    }


    public int remainingRetries(final ExternalTask externalTask) {
        RetryBehaviour retryBehaviour = new RetryBehaviour(externalTask, this.valueVault);
        return this.remainingRetries(retryBehaviour);
    }


    public int remainingRetries(final RetryBehaviour retryBehaviour) {
        if (retryBehaviour.hasRetries()) {
            return retryBehaviour.nextRetries();
        } else {
            return retryBehaviour.determineRetriesFromConfig();
        }
    }

    public long nextRetryInterval(final ExternalTask externalTask) {
        RetryBehaviour retryBehaviour = new RetryBehaviour(externalTask, this.valueVault);
        final int remainingRetries = this.remainingRetries(retryBehaviour);
        return retryBehaviour.nextRetryInterval(remainingRetries);
    }

}
