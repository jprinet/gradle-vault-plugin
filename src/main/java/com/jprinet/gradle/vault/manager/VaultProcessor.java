package com.jprinet.gradle.vault.manager;

import com.jprinet.gradle.vault.configuration.VaultConfigurationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class VaultProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultProcessor.class);

    private static final String TOKEN_SEPARATOR = "@@";
    private static final String TOKEN_PREFIX = "vault";

    static final String FILE_MISSING_SECRETS = ".vault_missing_secrets";
    static final String FILE_MISSING_SECRETS_SEPARATOR = ";";
    private static final String FILE_MISSING_SECRETS_SUFFIX = "fill_me";

    private final VaultConfigurationExtension configuration;

    public VaultProcessor(VaultConfigurationExtension configuration) {
        this.configuration = configuration;
    }

    /**
     * replace templates matching vault entry from file in path
     */
    public void process() {
        Pattern p = Pattern.compile(configuration.resourcePattern);

        try {
            Set<String> missingSecrets = new HashSet<>();

            Files.walk(Paths.get(configuration.resourcePath))
                 .filter(Files::isRegularFile)
                 .filter(path -> p.matcher(path.toString()).matches())
                 .forEach(path -> process(path, missingSecrets));

            if (!missingSecrets.isEmpty()) {
                try {
                    configuration.getIoManager().createFile(FILE_MISSING_SECRETS, new ArrayList<>(missingSecrets), false);
                } catch (IllegalStateException e) {
                    configuration.getIoManager().saveFile(FILE_MISSING_SECRETS, new ArrayList<>(missingSecrets), false);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("unable to process files", e);
        }
    }

    private void process(Path path, Set<String> missingSecrets) {
        List<String> output = new ArrayList<>();

        // loadFile vault
        Map<String, String> vaultContent = configuration.getVaultManager().getVaultContent();

        // read file
        List<String> content = configuration.getIoManager().loadFile(path.toString(), false);
        if (content != null && !content.isEmpty()) {
            content.forEach(line -> output.add(getProcessedLine(line, vaultContent, missingSecrets)));
        }

        // update file
        configuration.getIoManager().saveFile(path.toString(), output, false);
    }

    private String getProcessedLine(String line, Map<String, String> vaultContent, Set<String> missingSecrets) {
        if (line.contains(TOKEN_SEPARATOR)) {
            String[] tokens = line.split(TOKEN_SEPARATOR);
            for (String token : tokens) {
                if (token.startsWith(TOKEN_PREFIX)) {
                    String encryptedKey = configuration.getVaultManager().encrypt(token);
                    String encryptedValue = vaultContent.get(encryptedKey);
                    if (encryptedValue != null && !encryptedValue.isEmpty()) {
                        line = line.replaceAll(getProtectedKey(token), configuration.getVaultManager().decrypt(encryptedValue));
                    } else {
                        LOGGER.error("no value in vault for " + token);
                        missingSecrets.add(token + FILE_MISSING_SECRETS_SEPARATOR + FILE_MISSING_SECRETS_SUFFIX);
                    }
                }
            }
        }

        return line;
    }

    private String getProtectedKey(String key) {
        return TOKEN_SEPARATOR + key + TOKEN_SEPARATOR;
    }
}

