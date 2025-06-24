package com.enaya.product_service.application.mapper;

import com.enaya.product_service.application.dto.request.CreateCollectionRequest;
import com.enaya.product_service.application.dto.request.UpdateCollectionRequest;
import com.enaya.product_service.application.dto.response.CollectionResponse;
import com.enaya.product_service.domain.model.collection.Collection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollectionMapper {

    public CollectionResponse toResponse(Collection collection) {
        if (collection == null) {
            return null;
        }

        return CollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .slug(collection.getSlug())
                .period(collection.getPeriod())
                .images(collection.getImages())
                .productIds(collection.getProductIds())
                .metaTitle(collection.getMetaTitle())
                .metaDescription(collection.getMetaDescription())
                .priority(collection.getPriority())
                .active(collection.isActive())
                .published(collection.isPublished())
                .archived(!collection.isActive() && !collection.isPublished())
                .creationDate(collection.getCreationDate())
                .modificationDate(collection.getModificationDate())
                .version(collection.getVersion())
                .build();
    }

    public Collection toEntity(CreateCollectionRequest request) {
        if (request == null) {
            return null;
        }

        Collection collection;
        if (request.getPeriod() == null) {
            collection = Collection.createPermanent(request.getName(), request.getDescription());
        } else {
            collection = Collection.createSeasonal(
                    request.getName(),
                    request.getDescription(),
                    request.getPeriod().getStartDate(),
                    request.getPeriod().getEndDate()
            );
        }

        // Ajout des images
        if (request.getImages() != null) {
            request.getImages().forEach(collection::addImage);
        }

        // Ajout des produits
        if (request.getProductIds() != null) {
            collection.addProducts(request.getProductIds());
        }

        // Ajout des métadonnées
        if (request.getMetaTitle() != null || request.getMetaDescription() != null) {
            collection.updateMetadata(request.getMetaTitle(), request.getMetaDescription());
        }

        // Mise à jour de la priorité
        if (request.getPriority() != null) {
            collection.updatePriority(request.getPriority());
        }

        return collection;
    }

    public void updateEntity(Collection collection, UpdateCollectionRequest request) {
        if (collection == null || request == null) {
            return;
        }

        // Mise à jour des informations de base
        if (request.getName() != null || request.getDescription() != null) {
            collection.updateBasicInfo(
                    request.getName() != null ? request.getName() : collection.getName(),
                    request.getDescription() != null ? request.getDescription() : collection.getDescription()
            );
        }

        // Mise à jour de la période
        if (request.getPeriod() != null) {
            collection.updatePeriod(request.getPeriod());
        }

        // Mise à jour des images
        if (request.getImages() != null) {
            collection.getImages().clear();
            request.getImages().forEach(collection::addImage);
        }

        // Mise à jour des produits
        if (request.getProductIds() != null) {
            collection.clearProducts();
            collection.addProducts(request.getProductIds());
        }

        // Mise à jour des métadonnées
        if (request.getMetaTitle() != null || request.getMetaDescription() != null) {
            collection.updateMetadata(
                    request.getMetaTitle() != null ? request.getMetaTitle() : collection.getMetaTitle(),
                    request.getMetaDescription() != null ? request.getMetaDescription() : collection.getMetaDescription()
            );
        }

        // Mise à jour de la priorité
        if (request.getPriority() != null) {
            collection.updatePriority(request.getPriority());
        }

        // Mise à jour du statut actif
        if (request.getActive() != null) {
            if (request.getActive()) {
                collection.activate();
            } else {
                collection.deactivate();
            }
        }

        // Mise à jour du statut de publication
        if (request.getPublished() != null) {
            if (request.getPublished()) {
                collection.publish();
            } else {
                collection.unpublish();
            }
        }
    }

    public List<CollectionResponse> toResponseList(List<Collection> collections) {
        if (collections == null) {
            return List.of();
        }
        return collections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}