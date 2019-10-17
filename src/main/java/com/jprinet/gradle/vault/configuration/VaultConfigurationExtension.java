package com.jprinet.gradle.vault.configuration;

import com.jprinet.gradle.vault.manager.IOManager;
import com.jprinet.gradle.vault.manager.VaultManager;
import com.jprinet.gradle.vault.manager.VaultProcessor;

public class VaultConfigurationExtension {

    private static final String DEFAULT_VAULT_FILE = ".vault";
    private static final String DEFAULT_VAULT_PASSPHRASE_FILE = ".vault_passphrase";
    private static final String DEFAULT_RESOURCE_PATH = "build";
    private static final String DEFAULT_RESOURCE_PATTERN = ".*/conf/.*.properties";

    // plugin configuration
    public String vaultFile;
    public String vaultPassphraseFile;
    public String resourcePath;
    public String resourcePattern;

    private VaultManager vaultManager;
    private VaultProcessor vaultProcessor;
    private IOManager ioManager;

    /**
     * initialize context
     */
    public void init(boolean isVaultAccessProtected) {
        // parameters
        vaultFile = checkValue(vaultFile, DEFAULT_VAULT_FILE);
        vaultPassphraseFile = checkValue(vaultPassphraseFile, DEFAULT_VAULT_PASSPHRASE_FILE);
        resourcePath = checkValue(resourcePath, DEFAULT_RESOURCE_PATH);
        resourcePattern = checkValue(resourcePattern, DEFAULT_RESOURCE_PATTERN);

        // managers
        ioManager = new IOManager();
        vaultManager = new VaultManager(this);
        vaultProcessor = new VaultProcessor(this);

        if (isVaultAccessProtected) {
            vaultManager.assertVaultAccess();
        }
    }

    private String checkValue(String value, String defaultValue) {
        if ((value == null) || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public VaultProcessor getVaultProcessor() {
        return vaultProcessor;
    }

    public IOManager getIoManager() {
        return ioManager;
    }
}
