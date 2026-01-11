package com.example.cryptofile;

import java.time.LocalDateTime;

public class FileInfo {
    private int file_id;
    private int user_id;

    private String og_file_name;
    private Long og_file_size;
    private String og_file_type;
    private String og_file_hash;

    private String new_file_name;
    private Long new_file_size;
    private String encrypted_file_hash;


    private String status;
    private LocalDateTime encrypted_at;
    private LocalDateTime decrypted_at;
    private LocalDateTime modified_at;


    private Boolean deleted;
    private String username;

    public FileInfo() {
    }

    public int getFile_id() {
        return file_id;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getOg_file_name() {
        return og_file_name;
    }

    public void setOg_file_name(String og_file_name) {
        this.og_file_name = og_file_name;
    }


    public Long getOg_file_size() {
        return og_file_size;
    }

    public void setOg_file_size(Long og_file_size) {
        this.og_file_size = og_file_size;
    }

    public String getOg_file_type() {
        return og_file_type;
    }

    public void setOg_file_type(String og_file_type) {
        this.og_file_type = og_file_type;
    }

    public String getNew_file_name() {
        return new_file_name;
    }

    public void setNew_file_name(String new_file_name) {
        this.new_file_name = new_file_name;
    }

    public Long getNew_file_size() {
        return new_file_size;
    }

    public void setNew_file_size(Long new_file_size) {
        this.new_file_size = new_file_size;
    }

    public String getOg_file_hash() {
        return og_file_hash;
    }

    public void setOg_file_hash(String og_file_hash) {
        this.og_file_hash = og_file_hash;
    }

    public String getEncrypted_file_hash() {
        return encrypted_file_hash;
    }

    public void setEncrypted_file_hash(String encrypted_file_hash) {
        this.encrypted_file_hash = encrypted_file_hash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEncrypted_at() {
        return encrypted_at;
    }

    public void setEncrypted_at(LocalDateTime encrypted_at) {
        this.encrypted_at = encrypted_at;
    }

    public LocalDateTime getDecrypted_at() {
        return decrypted_at;
    }

    public void setDecrypted_at(LocalDateTime decrypted_at) {
        this.decrypted_at = decrypted_at;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public String getUserName() {
        return username;
    }

    public LocalDateTime getModified_at() {
        return modified_at;
    }

    public void setModified_at(LocalDateTime modified_at) {
        this.modified_at = modified_at;
    }
}
