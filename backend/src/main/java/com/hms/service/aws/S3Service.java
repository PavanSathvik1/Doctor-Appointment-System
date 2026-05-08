package com.hms.service.aws;

import com.hms.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

/**
 * Service for interacting with AWS S3 for file management.
 */
@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private static final String LOCAL_STORAGE_DIR = "local-storage/";

    public S3Service(@Autowired(required = false) S3Client s3Client,
                     @Autowired(required = false) S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Value("${app.aws.s3-bucket}")
    private String bucketName;

    /**
     * Uploads bytes to S3.
     */
    public String uploadFile(byte[] data, String contentType, String filename, String keyPrefix) {
        if (s3Client == null) {
            log.warn("S3Client is null (local dev running without AWS credentials). Saving to local disk.");
            String key = keyPrefix + "dummy-" + UUID.randomUUID() + "-" + filename;
            saveFileLocally(key, data);
            return key;
        }

        try {
            String extension = "";
            if (filename != null && filename.contains(".")) {
                extension = filename.substring(filename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String key = keyPrefix + uniqueFilename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
            log.info("Successfully uploaded data to S3: {}", key);

            return key;

        } catch (Exception e) {
            log.error("S3 upload failed", e);
            throw new AppException("Error uploading file to storage", HttpStatus.INTERNAL_SERVER_ERROR, "S3_UPLOAD_ERROR");
        }
    }

    /**
     * Uploads a file to S3 under the specified key prefix.
     */
    public String uploadFile(MultipartFile file, String keyPrefix) {
        if (s3Client == null) {
            log.warn("S3Client is null (local dev). Saving to local disk.");
            String key = keyPrefix + "dummy-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            try {
                saveFileLocally(key, file.getBytes());
            } catch (IOException e) {
                log.error("Failed to read file bytes for local storage", e);
            }
            return key;
        }

        try {
            // Generate unique filename to avoid overwrites
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String key = keyPrefix + uniqueFilename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("Successfully uploaded file to S3: {}", key);

            return key;

        } catch (IOException e) {
            log.error("Failed to read file for S3 upload", e);
            throw new AppException("Failed to process file upload", HttpStatus.INTERNAL_SERVER_ERROR, "FILE_READ_ERROR");
        } catch (Exception e) {
            log.error("S3 upload failed", e);
            throw new AppException("Error uploading file to storage", HttpStatus.INTERNAL_SERVER_ERROR, "S3_UPLOAD_ERROR");
        }
    }

    private void saveFileLocally(String key, byte[] bytes) {
        try {
            Path path = Paths.get(LOCAL_STORAGE_DIR + key);
            Files.createDirectories(path.getParent());
            Files.write(path, bytes);
            log.info("Stored file locally at: {}", path.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to save dummy file locally", e);
        }
    }

    /**
     * Uploads a file to S3 under the specified key prefix.

    /**
     * Generates a pre-signed URL for downloading an S3 object.
     * The URL expires in 15 minutes to guarantee security.
     *
     * @param key the S3 object key
     * @return the pre-signed URL string
     */
    public String generatePresignedUrl(String key) {
        if (s3Presigner == null) {
            log.warn("S3Presigner is null. Returning local file serving URL.");
            return "http://localhost:8080/api/files/view/" + key;
        }

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(b -> b.bucket(bucketName).key(key).build())
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
            
        } catch (Exception e) {
            log.error("Failed to generate pre-signed URL for key: {}", key, e);
            throw new AppException("Error generating download link");
        }
    }

    /**
     * Deletes an object from S3.
     *
     * @param key the S3 object key
     */
    public void deleteFile(String key) {
        if (s3Client == null) return;
        
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete S3 file with key: {}", key, e);
        }
    }
}
