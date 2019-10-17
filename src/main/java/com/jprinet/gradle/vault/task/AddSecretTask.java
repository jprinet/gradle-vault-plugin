package com.jprinet.gradle.vault.task;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;

public class AddSecretTask extends AbstractVaultTask {

    private static final String TASK_DESCRIPTION = "Add secret to vault";

    @Override
    public String getDescription() {
        return TASK_DESCRIPTION;
    }

    @Override
    protected void process(VaultConfigurationExtension configuration) {
        configuration.getVaultManager().addSecret();
    }
}
