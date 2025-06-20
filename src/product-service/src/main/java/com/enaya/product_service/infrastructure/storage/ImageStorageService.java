package com.enaya.product_service.infrastructure.storage;

import java.io.InputStream;
import java.util.Optional;

public interface ImageStorageService {
    String uploadImage(InputStream inputStream, String fileName, String contentType);
    Optional<InputStream> downloadImage(String fileName);
    void deleteImage(String fileName);
    String getImageUrl(String fileName);
}
