package com.enaya.product_service.infrastructure.persistence.repository;

import com.enaya.product_service.domain.model.collection.Collection;
import com.enaya.product_service.domain.repository.CollectionRepository;
import com.enaya.product_service.infrastructure.persistence.jpa.CollectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CollectionRepositoryImpl implements CollectionRepository {

    private final CollectionJpaRepository jpaRepository;

    @Override
    public Collection save(Collection collection) {
        return jpaRepository.save(collection);
    }

    @Override
    public Optional<Collection> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Collection> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Collection> findAllById(Iterable<UUID> ids) {
        return jpaRepository.findAllById(ids);
    }

    @Override
    public List<Collection> findActiveCollections() {
        return jpaRepository.findActiveCollections();
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public List<Collection> findByNameContaining(String name) {
        return jpaRepository.findByNameContaining(name);
    }

    @Override
    public List<Collection> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId);
    }
}
