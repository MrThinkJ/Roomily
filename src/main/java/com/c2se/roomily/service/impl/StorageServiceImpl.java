package com.c2se.roomily.service.impl;

import com.c2se.roomily.config.StorageConfig;
import com.c2se.roomily.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@AllArgsConstructor
public class StorageServiceImpl implements StorageService {
    MinioClient minioClient;
    StorageConfig storageConfig;

    @Override
    public void putObject(MultipartFile file, String bucket, String fileName) throws Exception {
        String contentType = file.getContentType();
        InputStream stream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .object(fileName)
                        .bucket(bucket)
                        .stream(stream, file.getSize(), -1)
                        .contentType(contentType)
                        .build()
        );
    }

    @Override
    public void putFolder(String folderName) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .object(folderName)
                        .bucket(storageConfig.getBucketStore())
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .build()
        );
    }

//    @Override
//    public List<Item> listFile(String bucket, String prefix) throws Exception {
//        List<Item>
//        Iterable<Result<Item>> items = minioClient.listObjects(
//                ListObjectsArgs.builder()
//                        .prefix(prefix)
//                        .bucket(bucket)
//                        .build()
//        );
//        return null;
//    }

    @Override
    public GetObjectResponse getObject(String bucket, String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }

    @Override
    public void removeObject(String bucket, String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }

    @Override
    public String generatePresignedUrl(String bucket, String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }
}
