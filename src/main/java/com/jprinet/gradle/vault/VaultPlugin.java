package com.jprinet.gradle.vault;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;
import com.jprinet.gradle.vault.task.AddMissingSecretsTask;
import com.jprinet.gradle.vault.task.AddSecretTask;
import com.jprinet.gradle.vault.task.CreateVaultTask;
import com.jprinet.gradle.vault.task.ProcessSecretsTask;
import com.jprinet.gradle.vault.task.RemoveSecretTask;
import com.jprinet.gradle.vault.task.RevealSecretTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VaultPlugin implements Plugin<Project> {

    private static final String TASK_PROCESS_VAULT = "processSecrets";
    private static final String TASK_CREATE_VAULT = "createVault";
    private static final String TASK_ADD_SECRET = "addSecret";
    private static final String TASK_ADD_MISSING_SECRET = "addMissingSecrets";
    private static final String TASK_REMOVE_SECRET = "removeSecret";
    private static final String TASK_REVEAL_SECRET = "revealSecret";

    @Override
    public void apply(Project project) {
        project.getExtensions().create("vault", VaultConfigurationExtension.class);

        project.getTasks().create(TASK_CREATE_VAULT, CreateVaultTask.class);
        project.getTasks().create(TASK_ADD_SECRET, AddSecretTask.class);
        project.getTasks().create(TASK_ADD_MISSING_SECRET, AddMissingSecretsTask.class);
        project.getTasks().create(TASK_REMOVE_SECRET, RemoveSecretTask.class);
        project.getTasks().create(TASK_REVEAL_SECRET, RevealSecretTask.class);
        project.getTasks().create(TASK_PROCESS_VAULT, ProcessSecretsTask.class);
    }
}