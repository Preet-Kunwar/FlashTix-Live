package com.flashtix.common.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Uploads a PDF to SeaweedFS (S3-compatible storage).
     * Auto-creates the bucket if it doesn't exist.
     *
     * @return path in the format "bucket-name/filename"
     */
    public String uploadTicketPdf(String fileName, byte[] pdfBytes) {
        log.info("[S3-STORAGE] Uploading '{}' to bucket='{}' (size={}KB)",
                fileName, bucketName, pdfBytes.length / 1024);

        try {
            // ── Ensure bucket exists ──────────────────────────────────────────────
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                log.info("[S3-STORAGE] Bucket '{}' not found — creating it now", bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("[S3-STORAGE] ✅ Bucket '{}' created", bucketName);
            } else {
                log.debug("[S3-STORAGE] Bucket '{}' already exists", bucketName);
            }

            // ── Upload file ───────────────────────────────────────────────────────
            InputStream inputStream = new ByteArrayInputStream(pdfBytes);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build()
            );

            String path = bucketName + "/" + fileName;
            log.info("[S3-STORAGE] ✅ Upload SUCCESSFUL — path='{}'", path);
            return path;

        } catch (Exception e) {
            log.error("[S3-STORAGE] ❌ Upload FAILED for '{}' — {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Error uploading file to S3-compatible storage: " + e.getMessage(), e);
        }
    }

    /**
     * Downloads a PDF from SeaweedFS by filename.
     * The caller is responsible for closing the returned InputStream.
     */
    public InputStream downloadTicketPdf(String fileName) {
        log.info("[S3-STORAGE] Downloading '{}' from bucket='{}'", fileName, bucketName);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("[S3-STORAGE] ✅ Download stream opened for '{}'", fileName);
            return stream;

        } catch (Exception e) {
            log.error("[S3-STORAGE] ❌ Download FAILED for '{}' — {}", fileName, e.getMessage());
            throw new RuntimeException("Error downloading file from S3-compatible storage: " + e.getMessage(), e);
        }
    }
}