package com.example.cryptofile;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
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

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cipherOutputStream = new CipherOutputStream(fos, cipher)) {

            fos.write(salt);
            fos.write(iv);

            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
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

    public static void decryptFile(String inputFile, String outputFile, String password, DoubleConsumer progressCallback) throws Exception {
        byte[] salt = new byte[Salt_LENGTH];
        byte[] iv = new byte[GCM_IV_LENGTH];

        long totalBytes = new File(inputFile).length() - Salt_LENGTH - GCM_IV_LENGTH;
        long processedBytes = 0;
        double lastReportedProgress = 0;

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            if (fis.read(salt) != Salt_LENGTH) {
                throw new IOException("Cannot read salt from file");
            }
            if (fis.read(iv) != GCM_IV_LENGTH) {
                throw new IOException("Cannot read IV from file");
            }

            SecretKey key = deriveKey(password, salt);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    processedBytes += bytesRead;

                    double progress = (double) processedBytes / totalBytes;
                    if (progress - lastReportedProgress >= 0.01 || progress >= 1.0) {
                        progressCallback.accept(progress);
                        lastReportedProgress = progress;
                    }
                }

                if (lastReportedProgress < 1.0) {
                    progressCallback.accept(1.0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during file decryption", e);
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


    // Customize the list view to show file name, size and remove button
    public static void customListview(ListView<File> listView, Label fileCountLabel, TextField outputFilePath) {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label labelName = new Label(item.getName());
                    Label labelSize = new Label("(" + item.length()/1024 + " KB)"); //TODO: create function for file size
                    VBox fileInfoBox = new VBox(labelName, labelSize);
                    fileInfoBox.setAlignment(Pos.CENTER_LEFT);

                    Button rmvbutton = new Button("Remove");

                    HBox row = new HBox();
                    row.getChildren().addAll(fileInfoBox, rmvbutton);
                    row.setSpacing(10);
                    HBox.setHgrow(fileInfoBox, Priority.ALWAYS);
                    row.setStyle("-fx-padding: 5px;");
                    setGraphic(row);

                    rmvbutton.setOnAction(event -> {
                        listView.getItems().remove(item);
                        updateListview(listView);
                        if(listView.getItems().isEmpty()) {
                            listView.setPrefHeight(0);
                        }
                        updateOutputPath(outputFilePath, listView);
                        fileCountLabel.setText("Selected files (" + listView.getItems().size() + ")");
                    });
                }
            }
        });
    }

    // Update the list view height based on number of items
    public static void updateListview(ListView<File> listView) {
        int size = listView.getItems().size();
        int visibleRows = Math.min(size, 3);
        listView.setPrefHeight(visibleRows * 50 + 2);
        listView.setStyle("-fx-padding: 5px;");
    }

    // Update output path if all files are from the same directory
    private static void updateOutputPath(TextField outputFilePath, ListView<File> listView) {
        if(outputFilePath!=null && !listView.getItems().isEmpty()) {
            String parentDir = listView.getItems().getFirst().getParent();
            boolean allSameDir = listView.getItems().stream().allMatch(f -> f.getParent().equals(parentDir));
            if(allSameDir) {
                outputFilePath.setText(parentDir);
            } else {
                outputFilePath.setText("");
            }
        }
    }

    // Browse and select files to encrypt and add the files to the list view
    public static void handleBrowseFiles(List<File> selectedFiles, ListView<File> listView, ObservableList<File> fileList,
                                         StackPane browseBox, Button removeAllBtn, Label fileCountLabel, TextField outputFilePath) { //TODO: add the drag and drop

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Encrypt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser. ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt", "*.xlsx", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.svg"),
                new FileChooser.ExtensionFilter("Videos", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv"),
                new FileChooser. ExtensionFilter("Audio", "*.mp3", "*.wav", "*.flac", "*.aac"),
                new FileChooser. ExtensionFilter("Archives", "*.zip", "*.rar", "*.7z", "*.tar", "*.gz")
        );

        selectedFiles = fileChooser.showOpenMultipleDialog(browseBox.getScene().getWindow());
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                listView.getItems().add(file);

            }
            if(listView.getItems().size() > 1) {
                removeAllBtn.setVisible(true);
            }
            fileList.setAll(listView.getItems());
            updateListview(listView);
            updateOutputPath(outputFilePath, listView);

            fileCountLabel.setText("Selected files (" + listView.getItems().size() + ")");
        }
    }

    // Browse and select output directory for encrypted files
    public static void handleBrowseOutputPath(ListView<File> listView, StackPane browseBox, Button removeAllBtn, TextField outputFilePath) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        if(!listView.getItems().isEmpty()) {
            String parentDir = listView.getItems().getFirst().getParent();
            boolean allSameDir = listView.getItems().stream().allMatch(f -> f.getParent().equals(parentDir));
            if(allSameDir) {
                directoryChooser.setInitialDirectory(new File(parentDir));
            }
        }

        File selectedDirectory = directoryChooser.showDialog(browseBox.getScene().getWindow());
        if (selectedDirectory != null) {
            outputFilePath.setText(selectedDirectory.getAbsolutePath());
        }
    }


    // Remove all files from the list view and reset related fields
    public static void removeAllFiles(ListView<File> listView, Label fileCountLabel, Button removeAllBtn, TextField outputFilePath) {
        listView.getItems().clear();
        updateListview(listView);
        updateOutputPath(outputFilePath, listView);
        listView.setPrefHeight(0);
        fileCountLabel.setText("Selected files (0)");
        removeAllBtn.setVisible(false);
    }

}
