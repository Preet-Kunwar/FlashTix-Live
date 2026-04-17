package com.flashtix.common.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public String uploadTicketPdf(String fileName, byte[] pdfBytes) {
        try {
            // 1. Check if the bucket exists, if not, create it!
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created new MinIO bucket: " + bucketName);
            }

            // 2. Upload the file
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build()
            );

            System.out.println("Successfully uploaded " + fileName + " to MinIO S3.");

            // 3. Return the URL path where the frontend can download it
            return bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Error occurred while uploading file to MinIO", e);
        }
    }

    // You will need to import io.minio.GetObjectArgs;

    public InputStream downloadTicketPdf(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from MinIO: " + e.getMessage());
        }
    }

}