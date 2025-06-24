package com.enaya.product_service.application.service;

import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import com.enaya.product_service.domain.ports.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageApplicationService {

    private final ImageStorageService imageStorageService;

    /**
     * Upload une image et crée un objet ImageCollection
     * @param file Le fichier image à uploader
     * @param altText Le texte alternatif pour l'image
     * @param type Le type d'image (BANNER, THUMBNAIL, HERO, BACKGROUND)
     * @param description La description de l'image (optionnel)
     * @return Un objet ImageCollection représentant l'image uploadée
     */
    public ImageCollection uploadImage(MultipartFile file, String altText, String type, String description) {
        try {
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;

            // Upload de l'image
            String imageUrl = imageStorageService.uploadImage(
                    file.getInputStream(),
                    uniqueFilename,
                    file.getContentType()
            );

            // Créer l'objet ImageCollection selon le type
            return switch (type.toUpperCase()) {
                case "BANNER" -> ImageCollection.banner(imageUrl, altText);
                case "HERO" -> ImageCollection.hero(imageUrl, altText, description);
                case "BACKGROUND" -> ImageCollection.background(imageUrl, altText);
                case "THUMBNAIL" -> ImageCollection.thumbnail(imageUrl, altText, 0, 0);
                default -> throw new IllegalArgumentException("Type d'image non supporté: " + type);
            };

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    /**
     * Upload une image avec des dimensions spécifiques
     * @param file Le fichier image à uploader
     * @param altText Le texte alternatif pour l'image
     * @param width La largeur de l'image
     * @param height La hauteur de l'image
     * @return Un objet ImageCollection représentant l'image uploadée
     */
    public ImageCollection uploadImageWithDimensions(MultipartFile file, String altText, int width, int height) {
        try {
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;

            // Upload de l'image
            String imageUrl = imageStorageService.uploadImage(
                    file.getInputStream(),
                    uniqueFilename,
                    file.getContentType()
            );

            // Créer un objet ImageCollection avec les dimensions spécifiées
            return ImageCollection.thumbnail(imageUrl, altText, width, height);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    /**
     * Upload une image avec tous les détails
     * @param file Le fichier image à uploader
     * @param altText Le texte alternatif pour l'image
     * @param displayOrder L'ordre d'affichage
     * @param type Le type d'image
     * @param width La largeur de l'image
     * @param height La hauteur de l'image
     * @param description La description de l'image
     * @return Un objet ImageCollection représentant l'image uploadée
     */
    public ImageCollection uploadImageWithFullDetails(
            MultipartFile file,
            String altText,
            int displayOrder,
            String type,
            int width,
            int height,
            String description) {
        try {
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "." + extension;

            // Upload de l'image
            String imageUrl = imageStorageService.uploadImage(
                    file.getInputStream(),
                    uniqueFilename,
                    file.getContentType()
            );

            // Créer un objet ImageCollection avec tous les détails
            return ImageCollection.withFullDetails(
                    imageUrl,
                    altText,
                    displayOrder,
                    type,
                    file.getSize(),
                    width,
                    height,
                    description
            );

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image", e);
        }
    }

    /**
     * Supprime une image
     * @param imageCollection L'objet ImageCollection contenant l'URL de l'image à supprimer
     */
    public void deleteImage(ImageCollection imageCollection) {
        if (imageCollection != null && imageCollection.getUrl() != null) {
            imageStorageService.deleteImage(imageCollection.getUrl());
        }
    }

    /**
     * Extrait l'extension d'un nom de fichier
     * @param filename Le nom du fichier
     * @return L'extension du fichier
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Le nom du fichier doit contenir une extension");
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
