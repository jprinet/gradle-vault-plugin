package com.jprinet.gradle.vault.task;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;

public class ProcessSecretsTask extends AbstractVaultTask {

    private static final String TASK_DESCRIPTION = "Replace secrets in resources";

    @Override
    public String getDescription() {
        return TASK_DESCRIPTION;
    }

    @Override
    protected void process(VaultConfigurationExtension configuration) {
        configuration.getVaultProcessor().process();
    }
}
