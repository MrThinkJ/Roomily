package com.c2se.roomily.controller;

import com.c2se.roomily.service.StorageService;
import io.minio.GetObjectResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/storage")
public class StorageController {
    StorageService storageService;

    @GetMapping("/objects/{bucket}/{objectName}")
    public ResponseEntity<GetObjectResponse> getObject(@PathVariable String bucket,
                                                       @PathVariable String objectName) throws Exception {
        return ResponseEntity.ok(storageService.getObject(bucket, objectName));
    }

    @GetMapping("/presigned-url/{bucket}/{objectName}")
    public ResponseEntity<String> generatePresignedUrl(@PathVariable String bucket,
                                                       @PathVariable String objectName) throws Exception {
        return ResponseEntity.ok(storageService.generatePresignedUrl(bucket, objectName));
    }

    @DeleteMapping("/objects/{bucket}/{objectName}")
    public ResponseEntity<Boolean> removeObject(@PathVariable String bucket,
                                                @PathVariable String objectName) throws Exception {
        storageService.removeObject(bucket, objectName);
        return ResponseEntity.ok(true);
    }
}
