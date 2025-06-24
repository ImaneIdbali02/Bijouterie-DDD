package com.enaya.product_service.application.service;

import com.enaya.product_service.application.dto.request.CreateCollectionRequest;
import com.enaya.product_service.application.dto.request.UpdateCollectionRequest;
import com.enaya.product_service.application.dto.response.CollectionResponse;
import com.enaya.product_service.application.mapper.CollectionMapper;
import com.enaya.product_service.domain.event.collection.CollectionArchived;
import com.enaya.product_service.domain.event.collection.CollectionPublished;
import com.enaya.product_service.domain.event.collection.CollectionUpdated;
import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.repository.CollectionRepository;
import com.enaya.product_service.domain.service.CollectionDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionApplicationService {

    private final CollectionRepository collectionRepository;
    private final CollectionDomainService collectionDomainService;
    private final CollectionMapper collectionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CollectionResponse createCollection(CreateCollectionRequest request) {
        // Check if collection with same name exists
        if (collectionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("A collection with this name already exists");
        }

        Collection collection = collectionMapper.toEntity(request);
        Collection savedCollection = collectionRepository.save(collection);
        return collectionMapper.toResponse(savedCollection);
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollection(UUID id) {
        return collectionRepository.findById(id)
                .map(collectionMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getAllCollections() {
        return collectionMapper.toResponseList(collectionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getActiveCollections() {
        return collectionMapper.toResponseList(collectionRepository.findActiveCollections());
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> searchCollections(String name) {
        return collectionMapper.toResponseList(collectionRepository.findByNameContaining(name));
    }

    @Transactional
    public CollectionResponse updateCollection(UUID id, UpdateCollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionMapper.updateEntity(collection, request);
        Collection updatedCollection = collectionRepository.save(collection);
        
        // Publish collection updated event
        eventPublisher.publishEvent(CollectionUpdated.from(updatedCollection));

        return collectionMapper.toResponse(updatedCollection);
    }

    @Transactional
    public void deleteCollection(UUID id) {
        if (!collectionRepository.existsById(id)) {
            throw new IllegalArgumentException("Collection not found");
        }
        collectionRepository.delete(id);
    }

    @Transactional
    public CollectionResponse publishCollection(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        if (!collectionDomainService.canBePublished(collection)) {
            throw new IllegalStateException("Collection cannot be published");
        }

        collectionDomainService.publishCollection(collection);
        Collection publishedCollection = collectionRepository.save(collection);
        
        // Publish collection published event
        eventPublisher.publishEvent(CollectionPublished.from(publishedCollection));

        return collectionMapper.toResponse(publishedCollection);
    }

    @Transactional
    public CollectionResponse archiveCollection(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionDomainService.archiveCollection(collection);
        Collection archivedCollection = collectionRepository.save(collection);
        
        // Publish collection archived event
        eventPublisher.publishEvent(CollectionArchived.from(archivedCollection.getId()));

        return collectionMapper.toResponse(archivedCollection);
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> getCollectionsByProduct(UUID productId) {
        return collectionMapper.toResponseList(collectionRepository.findByProductId(productId));
    }

    @Transactional
    public CollectionResponse addProductToCollection(UUID collectionId, UUID productId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionDomainService.addProductToCollection(collection, productId);
        Collection updatedCollection = collectionRepository.save(collection);
        
        // Publish collection updated event
        eventPublisher.publishEvent(CollectionUpdated.from(updatedCollection));

        return collectionMapper.toResponse(updatedCollection);
    }

    @Transactional
    public CollectionResponse removeProductFromCollection(UUID collectionId, UUID productId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));

        collectionDomainService.removeProductFromCollection(collection, productId);
        Collection updatedCollection = collectionRepository.save(collection);
        
        // Publish collection updated event
        eventPublisher.publishEvent(CollectionUpdated.from(updatedCollection));

        return collectionMapper.toResponse(updatedCollection);
    }
}
