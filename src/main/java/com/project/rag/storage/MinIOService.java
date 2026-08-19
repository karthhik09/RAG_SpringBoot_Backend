package com.project.rag.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile file) {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

            String contentType = file.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "application/octet-stream"; // default generic binary type
            }

            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(uniqueFilename)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }
            return uniqueFilename;
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }
}
