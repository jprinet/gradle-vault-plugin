package com.jprinet.gradle.vault.manager;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class VaultManager {

    private static final String ENV_KEY_VAULT_PASSPHRASE = "VAULT_PASSPHRASE";
    private static final String SUPPORTED_VERSION = "V1.0";

    private static final String VAULT_SEPARATOR = "---";

    private final EncryptionManager encryptionManager;
    private final String vaultPassphrase;
    private final Map<String, String> vaultContent;
    private final VaultConfigurationExtension configuration;

    public VaultManager(VaultConfigurationExtension configuration) {
        this.configuration = configuration;
        this.vaultPassphrase = getVaultPassphrase(configuration.vaultPassphraseFile);
        this.vaultContent = getVaultContent();
        this.encryptionManager = new EncryptionManager(vaultPassphrase);
    }

    private String getVaultPassphrase(String vaultPassphraseFile) {
        // read environment variable
        String passPhrase = configuration.getIoManager().getEnvironmentVariable(ENV_KEY_VAULT_PASSPHRASE);
        if (null == passPhrase || passPhrase.isEmpty()) {
            // read file
            List<String> passphraseAsList = configuration.getIoManager().loadFile(vaultPassphraseFile, false);
            if (null != passphraseAsList && !passphraseAsList.isEmpty()) {
                passPhrase = String.join("", passphraseAsList);
            }
        }

        return passPhrase;
    }

    Map<String, String> getVaultContent() {
        List<String> content = configuration.getIoManager().loadFile(configuration.vaultFile, true);
        if (content != null) {
            return content.stream()
                          .map(this::parse)
                          .filter(Objects::nonNull)
                          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return Collections.emptyMap();
        }
    }

    private Map.Entry<String, String> parse(String line) {
        String[] tokens = line.split(VAULT_SEPARATOR);
        if (tokens.length == 2) {
            return new AbstractMap.SimpleEntry<>(tokens[0], tokens[1]);
        }

        return null;
    }

    /**
     * create vault
     */
    public void createVault(){
        if (null == vaultPassphrase || vaultPassphrase.isEmpty()) {
            throw new IllegalStateException("unable to create vault without passphrase");
        }

        String encryptedVaultPassphrase = encryptionManager.encrypt(vaultPassphrase);
        String encryptedVersion = encryptionManager.encrypt(SUPPORTED_VERSION);

        Map<String, String> content = new HashMap<>();
        content.put(encryptedVaultPassphrase, encryptedVersion);

        configuration.getIoManager().createFile(configuration.vaultFile, format(content), true);
    }

    /**
     * check vault passphrase is valid
     */
    public void assertVaultAccess() {
        if(vaultContent != null && !vaultContent.isEmpty()){
            String encryptedVaultPassphrase = encryptionManager.encrypt(vaultPassphrase);
            String encryptedVersion = encryptionManager.encrypt(SUPPORTED_VERSION);
            if (encryptedVersion.equals(vaultContent.get(encryptedVaultPassphrase))) {
               return;
            }
        }

        throw new IllegalStateException("vault access denied");
    }

    /**
     * add secret to vault
     */
    public void addSecret(){
        // check vault access
        assertVaultAccess();

        // collect user data
        String identifier = configuration.getIoManager().ask("Enter secret identifier:");
        String value = configuration.getIoManager().ask("Enter secret value:");

        addSecretInternal(identifier, value);
    }

    /**
     * add secret to vault
     */
    public void addMissingSecrets() {
        // check vault access
        assertVaultAccess();

        // read missing entries
        List<String> missingSecrets = configuration.getIoManager().loadFile(VaultProcessor.FILE_MISSING_SECRETS, false);
        if (null != missingSecrets && !missingSecrets.isEmpty()) {
            missingSecrets.forEach(line -> {
                String[] tokens = line.split(VaultProcessor.FILE_MISSING_SECRETS_SEPARATOR);
                if (tokens.length == 2) {
                    addSecretInternal(tokens[0], tokens[1]);
                } else {
                    throw new IllegalStateException("unable to process " + line);
                }
            });
        } else {
            throw new IllegalStateException("no missing secret found");
        }
    }

    private void addSecretInternal(String identifier, String value) {
        String encryptedIdentifier = encryptionManager.encrypt(identifier);
        String encryptedValue = encryptionManager.encrypt(value);

        // add secret
        vaultContent.put(encryptedIdentifier, encryptedValue);

        // save vault
        configuration.getIoManager().saveFile(configuration.vaultFile, format(vaultContent), true);
    }

    /**
     * remove secret from vault
     */
    public void removeSecret(){
        // check vault access
        assertVaultAccess();

        // collect user data
        String identifier = configuration.getIoManager().ask("Enter secret identifier:");
        String encryptedIdentifier = encryptionManager.encrypt(identifier);

        // remove secret
        vaultContent.remove(encryptedIdentifier);

        // save vault
        configuration.getIoManager().saveFile(configuration.vaultFile, format(vaultContent), true);
    }

    private List<String> format(Map<String, String> content) {
        List<String> output = new ArrayList<>();
        for (Map.Entry<String, String> entry : content.entrySet()) {
            output.add(entry.getKey() + VAULT_SEPARATOR + entry.getValue());
        }
        return output;
    }

    /**
     * reveal secret from vault
     */
    public void revealSecret(){
        // check vault access
        assertVaultAccess();

        // collect user data
        String identifier = configuration.getIoManager().ask("Enter secret identifier:");
        String encryptedIdentifier = encryptionManager.encrypt(identifier);

        // display secret
        System.out.println(encryptionManager.decrypt(vaultContent.getOrDefault(encryptedIdentifier, "NOT FOUND")));
    }

    String encrypt(String text) {
        return encryptionManager.encrypt(text);
    }

    String decrypt(String text) {
        return encryptionManager.decrypt(text);
    }
}
