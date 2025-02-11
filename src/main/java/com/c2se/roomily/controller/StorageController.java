package com.c2se.roomily.controller;

import com.c2se.roomily.service.StorageService;
import io.minio.GetObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/storage")
public class StorageController {
    StorageService storageService;
    @GetMapping("/objects")
    public ResponseEntity<GetObjectResponse> getObject(String bucket, String objectName) throws Exception {
        return ResponseEntity.ok(storageService.getObject(bucket, objectName));
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<String> generatePresignedUrl(String bucket, String objectName) throws Exception {
        return ResponseEntity.ok(storageService.generatePresignedUrl(bucket, objectName));
    }

    @DeleteMapping("/objects")
    public ResponseEntity<Boolean> removeObject(String bucket, String objectName) throws Exception {
        storageService.removeObject(bucket, objectName);
        return ResponseEntity.ok(true);
    }
}
