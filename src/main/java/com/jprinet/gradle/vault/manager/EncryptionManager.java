package com.jprinet.gradle.vault.manager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class EncryptionManager {

    // we need to reuse the same key over time to compute the same encryption/decryption
    private static final byte[] keyAsBytes = new byte[]{122, -116, -7, -6, -102, -52, -4, 34, 77, -109, 111, 99, -102, -126, -124, 76};
    private static final byte[] initialisationVectorAsBytes = new byte[]{-116, -7, -6, -102, -52, -4, 34, 77, -109, 111, 99, -102, -126, -124, 76, 122};

    private static final String SALT = "pepper";

    private final String passphrase;
    private final Key aesKey;
    private final IvParameterSpec iv;
    private final Cipher cipher;

    EncryptionManager(String passphrase) {
        try {
            this.passphrase = passphrase;
            this.aesKey = new SecretKeySpec(keyAsBytes, "AES");
            this.iv = new IvParameterSpec(initialisationVectorAsBytes);
            this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("unable to initialise encryption", e);
        }
    }

    /**
     * encrypt a secret
     *
     * @param secret secret to encrypt
     *
     * @return encrypted secret
     */
    String encrypt(String secret) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);
            byte[] encrypted = cipher.doFinal((secret + SALT + passphrase).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("unable to encrypt", e);
        }
    }

    /**
     * decrypt a secret
     *
     * @param encryptedSecret secret to decrypt
     *
     * @return decrypted secret
     */
    String decrypt(String encryptedSecret) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, aesKey, iv);
            byte[] decodeBase64 = Base64.getDecoder().decode(encryptedSecret);
            byte[] decrypted = cipher.doFinal(decodeBase64);
            String decryptedAsString = new String(decrypted, StandardCharsets.UTF_8);
            if (decryptedAsString.endsWith(SALT + passphrase)) {
                decryptedAsString = decryptedAsString.substring(0, decryptedAsString.length() - SALT.length() - passphrase.length());
            } else {
                throw new IllegalStateException("unable to decrypt");
            }

            return decryptedAsString;
        } catch (IllegalArgumentException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("unable to decrypt", e);
        }
    }

    /**
     * Key generator used once to initialise the aes key
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecretKey skey = kgen.generateKey();
        try (FileOutputStream out = new FileOutputStream("aes.key")) {
            byte[] keyb = skey.getEncoded();
            out.write(keyb);
        }
    }
}
