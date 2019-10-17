package com.jprinet.gradle.vault.task;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public abstract class AbstractVaultTask extends DefaultTask {

    private static final String PLUGIN_GROUP = "vault";

    @Override
    public String getGroup() {
        return PLUGIN_GROUP;
    }

    @TaskAction
    public void taskAction() {
        VaultConfigurationExtension configuration = getProject().getExtensions().findByType(VaultConfigurationExtension.class);
        if (null == configuration) {
            configuration = new VaultConfigurationExtension();
        }
        configuration.init(isVaultAccessProtected());

        process(configuration);
    }

    protected abstract void process(VaultConfigurationExtension configuration);

    /**
     * @return whether vault access has to be checked
     */
    protected boolean isVaultAccessProtected(){
        return true;
    }

}
