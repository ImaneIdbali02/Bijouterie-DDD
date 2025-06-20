package com.enaya.product_service.infrastructure.storage.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.enaya.product_service.domain.ports.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ImageStorageService implements ImageStorageService {

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadImage(InputStream inputStream, String fileName, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            // If content length is known, set it: metadata.setContentLength(inputStream.available());
            s3Client.putObject(bucketName, fileName, inputStream, metadata);
            log.info("Image uploaded to S3: {}", fileName);
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            log.error("Error uploading image to S3 {}: {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            String fileName = extractFileNameFromUrl(imageUrl);
            s3Client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
            log.info("Image deleted from S3: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting image from S3 {}: {}", imageUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to delete image from S3", e);
        }
    }

    private String extractFileNameFromUrl(String imageUrl) {
        // Extract the file name from the S3 URL
        // Example URL: https://bucket-name.s3.region.amazonaws.com/file-name.jpg
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }
}
