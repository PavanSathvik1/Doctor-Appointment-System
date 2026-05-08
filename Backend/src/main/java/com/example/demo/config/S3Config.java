package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configuration for AWS S3 Services (v2 SDK).
 */
@Configuration
public class S3Config {

    @Value("${app.aws.access-key}")
    private String accessKey;

    @Value("${app.aws.secret-key}")
    private String secretKey;

    @Value("${app.aws.region}")
    private String regionStr;

    /**
     * Configures the synchronous S3Client for uploads and deletes.
     */
    @Bean
    public S3Client s3Client() {
        if (accessKey == null || accessKey.isBlank()) {
            return null; // Handle local dev without credentials gracefully
        }
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .region(Region.of(regionStr))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * Configures the S3Presigner for generating time-limited download URLs.
     */
    @Bean
    public S3Presigner s3Presigner() {
        if (accessKey == null || accessKey.isBlank()) {
            return null;
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Presigner.builder()
                .region(Region.of(regionStr))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
