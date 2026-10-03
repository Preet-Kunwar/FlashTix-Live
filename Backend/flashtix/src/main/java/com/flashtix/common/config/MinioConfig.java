package com.flashtix.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * S3-Compatible Storage Configuration.
 *
 * Originally configured for MinIO. Since MinIO CE stopped publishing free
 * Docker images in October 2025, SeaweedFS is used as the S3 backend.
 * SeaweedFS is Apache 2.0 licensed, fully S3-compatible, and works directly
 * with the MinIO Java SDK.
 *
 * SeaweedFS S3 endpoint: http://localhost:8333
 *
 * IMPORTANT: SeaweedFS (and all non-AWS S3 providers) require path-style access.
 * This means URLs like: http://localhost:8333/flashtix-assets/ticket_order_1.pdf
 * instead of virtual-hosted style: http://flashtix-assets.localhost:8333/...
 */
@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * MinioClient configured for SeaweedFS.
     * The MinIO Java SDK works seamlessly with any S3-compatible endpoint.
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                // path-style is the default in MinioClient (it always uses path-style),
                // so no extra flag is needed here — just the correct endpoint URL.
                .build();
    }
}