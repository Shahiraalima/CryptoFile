package com.example.cryptofile;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.function.DoubleConsumer;

public class EncryptAndDecryptUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int Salt_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final int BUFFER_SIZE = 1024;

    public static void encryptFile(String inputFile, String outputFile, String password, DoubleConsumer progressCallback) throws Exception {
        byte[] salt = generateSalt();
        byte[] iv = generateIV();

        SecretKey key;
        try {
            key = deriveKey(password, salt);
        } catch (Exception e) {
            throw new RuntimeException("Error deriving key", e);
        }

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

//        byte[] fileData = readFile(inputFile);
//        byte[] encryptedData = cipher.doFinal(fileData);

        byte[] buffer = new byte[BUFFER_SIZE];
        long totalbytes = new File(inputFile).length();
        long processedBytes = 0;
        double lastReportedProgress = 0;

        try(FileInputStream fis = new FileInputStream(inputFile);
           FileOutputStream fos = new FileOutputStream(outputFile);
           CipherOutputStream cipherOutputStream = new CipherOutputStream(fos, cipher)) {

            fos.write(salt);
            fos.write(iv);

            int bytesRead;
            while((bytesRead = fis.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
                processedBytes += bytesRead;

                double progress = (double) processedBytes / totalbytes;
                if (progress - lastReportedProgress >= 0.01 || progress >= 1.0) {
                    progressCallback.accept(progress);
                    lastReportedProgress = progress;
                }
            }
            if (lastReportedProgress < 1.0) {
                progressCallback.accept(1.0);
            }

            cipherOutputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("Error during file encryption", e);
        }
    }


    public void decryptFile(String inputFile, String outputFile, String password) throws Exception {
        byte[] fileData = readFile(inputFile);

        byte[] salt = new byte[Salt_LENGTH];
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] encryptedData = new byte[fileData.length - Salt_LENGTH - GCM_IV_LENGTH];

        System.arraycopy(fileData, 0, salt, 0, Salt_LENGTH);
        System.arraycopy(fileData, Salt_LENGTH, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(fileData, Salt_LENGTH + GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);

        SecretKey key = deriveKey(password, salt);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error writing decrypted file", e);
        }
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[Salt_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey key = factory.generateSecret(spec);
        return new SecretKeySpec(key.getEncoded(), ALGORITHM);
    }

    private static byte[] readFile(String filePath) {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileData);
            return fileData;
        } catch (Exception e) {
            throw new RuntimeException("Error reading file", e);
        }
    }


}
