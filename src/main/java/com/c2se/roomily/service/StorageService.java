package com.c2se.roomily.service;

import io.minio.GetObjectResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    void putObject(MultipartFile file, String bucket, String fileName) throws Exception;
    void putObject(byte[] data, String bucket, String fileName, String contentType) throws Exception;
    void putObject(InputStream inputStream, String bucket, String fileName, String contentType) throws Exception;
    void putFolder(String folderName) throws Exception;

    //    List<Item> listFile(String bucket, String prefix) throws Exception;
    GetObjectResponse getObject(String bucket, String objectName) throws Exception;

    void removeObject(String bucket, String objectName) throws Exception;

    String generatePresignedUrl(String bucket, String objectName) throws Exception;
}
