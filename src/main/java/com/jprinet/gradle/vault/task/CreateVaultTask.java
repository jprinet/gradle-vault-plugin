package com.jprinet.gradle.vault.task;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;

public class CreateVaultTask extends AbstractVaultTask {

    private static final String TASK_DESCRIPTION = "Create vault";

    @Override
    public String getDescription() {
        return TASK_DESCRIPTION;
    }

    @Override
    protected void process(VaultConfigurationExtension configuration) {
        configuration.getVaultManager().createVault();
    }

    /**
     * @return false as access can't be checked if vault does not exist
     */
    @Override
    protected boolean isVaultAccessProtected() {
        return false;
    }
}
