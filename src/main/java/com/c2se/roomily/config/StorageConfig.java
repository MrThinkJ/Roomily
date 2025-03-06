package com.c2se.roomily.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class StorageConfig {
    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;
    @Value("${minio.bucket.image}")
    private String bucketStore;
    @Value("${minio.url}")
    private String url;

    @Bean
    public MinioClient minioClient() throws Exception {
        MinioClient minioClient = MinioClient.builder()
                .credentials(accessKey, secretKey)
                .endpoint(url)
                .build();
        initBucket(minioClient, bucketStore);
        return minioClient;
    }

    private void initBucket(MinioClient minioClient, String bucketStore) throws Exception {
        boolean exist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketStore).build());
        if (!exist)
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketStore).build());
    }
}
