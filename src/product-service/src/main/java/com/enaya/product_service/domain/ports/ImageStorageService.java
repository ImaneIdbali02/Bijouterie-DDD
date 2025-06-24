package com.enaya.product_service.domain.ports;

import java.io.InputStream;

public interface ImageStorageService {
    /**
     * Upload une image et retourne son URL
     * @param inputStream Le flux de l'image
     * @param fileName Le nom du fichier
     * @param contentType Le type de contenu (MIME type)
     * @return L'URL de l'image uploadée
     */
    String uploadImage(InputStream inputStream, String fileName, String contentType);

    /**
     * Supprime une image à partir de son URL
     * @param imageUrl L'URL de l'image à supprimer
     */
    void deleteImage(String imageUrl);
} 