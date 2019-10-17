package com.jprinet.gradle.vault.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class IOManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOManager.class);

    /**
     * Create a file
     *
     * @param filename name of the file to create
     * @param content file content
     * @param isBase64Encoded whether content has to be encoded in Base64 or not
     */
    void createFile(String filename, List<String> content, boolean isBase64Encoded) {
        if (Files.exists(Paths.get(filename))) {
            throw new IllegalStateException(filename + " already present");
        }

        if (isBase64Encoded) {
            saveFileInternal(filename, toBase64(content));
        } else {
            saveFileInternal(filename, content);
        }
    }

    /**
     * Update a file
     *
     * @param filename name of the file to update
     * @param content file content
     * @param isBase64Encoded whether content has to be encoded in Base64 or not
     */
    void saveFile(String filename, List<String> content, boolean isBase64Encoded) {
        if (!Files.exists(Paths.get(filename))) {
            throw new IllegalStateException(filename + " not present");
        }

        if (isBase64Encoded) {
            saveFileInternal(filename, toBase64(content));
        } else {
            saveFileInternal(filename, content);
        }
    }

    private List<String> toBase64(List<String> items) {
        return Collections.singletonList(
            Base64.getEncoder()
                  .encodeToString(
                      String.join("\n", items)
                            .getBytes(StandardCharsets.UTF_8)
                  )
        );
    }

    private void saveFileInternal(String filename, List<String> content) {
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            if (null != content) {
                content.forEach(writer::println);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new IllegalStateException("unable to save " + filename, e);
        }
    }

    /**
     * load a file
     *
     * @param filename file to load
     * @return file content
     * @param isBase64Encoded whether content is encoded in Base64 or not
     */
    List<String> loadFile(String filename, boolean isBase64Encoded) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
            if (isBase64Encoded) {
                return fromBase64(lines.get(0));
            } else {
                return lines;
            }
        } catch (IOException e) {
            LOGGER.info("unable to read " + filename, e);
        }

        return null;
    }

    private List<String> fromBase64(String content) {
        byte[] decodedBytes = Base64.getDecoder().decode(content);
        String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
        return Arrays.asList(decodedString.split("[\\r\\n]+"));
    }

    /**
     * read environment variable
     *
     * @param key environment variable key
     * @return environment variable value
     */
    String getEnvironmentVariable(String key) {
        return System.getenv(key);
    }

    /**
     * Ask a question to the user
     *
     * @param message message to display
     *
     * @return user's answer
     */
    String ask(String message) {
        System.out.println(message);
        try {
            // we can't close System.in otherwise next read attempt would fail
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            return br.readLine();
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to read user inputs", e);
        }
    }

}
