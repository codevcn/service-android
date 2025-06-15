package com.example.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file-android")
public class FileStoragePropertiesForAndroid {
    private String uploadDirForAndroid;

    public String getUploadDirForAndroid() {
        return uploadDirForAndroid;
    }

    public void setUploadDirForAndroid(String uploadDirForAndroid) {
        this.uploadDirForAndroid = uploadDirForAndroid;
    }
} 