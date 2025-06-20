package com.enaya.product_service.domain.repository;

import com.enaya.product_service.domain.model.collection.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository {
    Collection save(Collection collection);
    Optional<Collection> findById(UUID id);
    List<Collection> findAll();
    List<Collection> findAllById(Iterable<UUID> ids);
    List<Collection> findActiveCollections();
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    List<Collection> findByNameContaining(String name);
    List<Collection> findByProductId(UUID productId);
}
