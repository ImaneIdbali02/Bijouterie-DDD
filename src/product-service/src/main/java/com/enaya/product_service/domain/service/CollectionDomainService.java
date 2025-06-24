package com.enaya.product_service.domain.service;

import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import com.enaya.product_service.domain.model.collection.valueobjects.PeriodCollection;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CollectionDomainService {

    /**
     * Vérifie si une collection peut être publiée selon ses règles internes et d'autres contraintes métier.
     */
    public boolean canBePublished(Collection collection) {
        return collection.isActive()
                && !collection.isPublished()
                && (collection.isPermanent() || collection.isCurrentlyActive());
    }

    /**
     * Crée une collection permanente
     */
    public Collection createPermanentCollection(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        return Collection.createPermanent(name, description);
    }

    /**
     * Crée une collection saisonnière
     */
    public Collection createSeasonalCollection(String name, String description, 
                                             LocalDateTime startDate, LocalDateTime endDate) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Collection name cannot be null or empty");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        return Collection.createSeasonal(name, description, startDate, endDate);
    }

    /**
     * Vérifie l'unicité du slug dans une liste de collections existantes (simule un repository).
     */
    public boolean isSlugUnique(String slug, List<Collection> existingCollections) {
        return existingCollections.stream()
                .noneMatch(collection -> collection.getSlug().equalsIgnoreCase(slug));
    }

    /**
     * Met à jour les informations de base d'une collection
     */
    public void updateCollectionBasicInfo(Collection collection, String name, String description) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.updateBasicInfo(name, description);
    }

    /**
     * Met à jour la période d'une collection
     */
    public void updateCollectionPeriod(Collection collection, PeriodCollection newPeriod) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        if (newPeriod != null && newPeriod.getEndDate().isBefore(newPeriod.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        collection.updatePeriod(newPeriod);
    }

    /**
     * Met à jour les métadonnées d'une collection
     */
    public void updateCollectionMetadata(Collection collection, String metaTitle, String metaDescription) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.updateMetadata(metaTitle, metaDescription);
    }

    /**
     * Met à jour la priorité d'une collection
     */
    public void updateCollectionPriority(Collection collection, int priority) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.updatePriority(priority);
    }

    /**
     * Ajoute une image à une collection
     */
    public void addCollectionImage(Collection collection, ImageCollection image) {
        if (collection == null || image == null) {
            throw new IllegalArgumentException("Collection and image cannot be null");
        }
        collection.addImage(image);
    }

    /**
     * Supprime une image d'une collection
     */
    public void removeCollectionImage(Collection collection, String imageUrl) {
        if (collection == null || imageUrl == null) {
            throw new IllegalArgumentException("Collection and image URL cannot be null");
        }
        collection.removeImage(imageUrl);
    }

    /**
     * Ajoute un produit à une collection
     */
    public void addProductToCollection(Collection collection, UUID productId) {
        if (collection == null || productId == null) {
            throw new IllegalArgumentException("Collection and product ID cannot be null");
        }
        collection.addProduct(productId);
    }

    /**
     * Supprime un produit d'une collection
     */
    public void removeProductFromCollection(Collection collection, UUID productId) {
        if (collection == null || productId == null) {
            throw new IllegalArgumentException("Collection and product ID cannot be null");
        }
        collection.removeProduct(productId);
    }

    /**
     * Ajoute plusieurs produits à une collection
     */
    public void addProductsToCollection(Collection collection, List<UUID> productIds) {
        if (collection == null || productIds == null) {
            throw new IllegalArgumentException("Collection and product IDs cannot be null");
        }
        collection.addProducts(productIds);
    }

    /**
     * Vide la liste des produits d'une collection
     */
    public void clearCollectionProducts(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.clearProducts();
    }

    /**
     * Publie une collection
     */
    public void publishCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        if (!collection.isActive()) {
            throw new IllegalStateException("Cannot publish an inactive collection");
        }
        if (collection.isSeasonal() && !collection.isCurrentlyActive()) {
            throw new IllegalStateException("Cannot publish collection outside its active period");
        }
        collection.publish();
    }

    /**
     * Dépublie une collection
     */
    public void unpublishCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.unpublish();
    }

    /**
     * Active une collection
     */
    public void activateCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.activate();
    }

    /**
     * Désactive une collection
     */
    public void deactivateCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.deactivate();
    }

    /**
     * Archive une collection
     */
    public void archiveCollection(Collection collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        collection.archive();
    }

    /**
     * Vérifie si une collection est visible
     */
    public boolean isCollectionVisible(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isVisible();
    }

    /**
     * Vérifie si une collection est actuellement active
     */
    public boolean isCollectionCurrentlyActive(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isCurrentlyActive();
    }

    /**
     * Vérifie si une collection contient des produits
     */
    public boolean hasCollectionProducts(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.hasProducts();
    }

    /**
     * Vérifie si une collection contient un produit spécifique
     */
    public boolean containsProduct(Collection collection, UUID productId) {
        if (collection == null || productId == null) {
            return false;
        }
        return collection.containsProduct(productId);
    }

    /**
     * Obtient le nombre de produits dans une collection
     */
    public int getCollectionProductCount(Collection collection) {
        if (collection == null) {
            return 0;
        }
        return collection.getProductCount();
    }

    /**
     * Vérifie si une collection est permanente
     */
    public boolean isPermanentCollection(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isPermanent();
    }

    /**
     * Vérifie si une collection est saisonnière
     */
    public boolean isSeasonalCollection(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isSeasonal();
    }

    /**
     * Vérifie si une collection est expirée
     */
    public boolean isExpiredCollection(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isExpired();
    }

    /**
     * Vérifie si une collection est à venir
     */
    public boolean isUpcomingCollection(Collection collection) {
        if (collection == null) {
            return false;
        }
        return collection.isUpcoming();
    }

    /**
     * Obtient l'image principale d'une collection
     */
    public ImageCollection getCollectionPrimaryImage(Collection collection) {
        if (collection == null) {
            return null;
        }
        return collection.getPrimaryImage();
    }

    /**
     * Obtient les images visibles d'une collection
     */
    public List<ImageCollection> getCollectionVisibleImages(Collection collection) {
        if (collection == null) {
            return List.of();
        }
        return collection.getVisibleImages();
    }
}
