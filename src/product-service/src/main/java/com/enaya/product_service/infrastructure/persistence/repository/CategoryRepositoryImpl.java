package com.enaya.product_service.infrastructure.persistence.repository;

import com.enaya.product_service.domain.model.category.Category;
import com.enaya.product_service.domain.repository.CategoryRepository;
import com.enaya.product_service.infrastructure.persistence.jpa.CategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    @Override
    public Category save(Category category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Category> findActiveCategories() {
        return jpaRepository.findActiveCategories();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpaRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> findRootCategories() {
        return jpaRepository.findRootCategories();
    }

    @Override
    public List<Category> findLeafCategories() {
        return jpaRepository.findLeafCategories();
    }

    @Override
    public List<Category> findVisibleInMenu() {
        return jpaRepository.findVisibleInMenu();
    }

    @Override
    public List<Category> findByNameContaining(String name) {
        return jpaRepository.findByNameContaining(name);
    }

    @Override
    public List<Category> findByPathContaining(String path) {
        return jpaRepository.findByPathContaining(path);
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
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public List<Category> findDescendants(UUID ancestorId) {
        return jpaRepository.findDescendants(ancestorId);
    }

    @Override
    public List<Category> findAncestors(UUID descendantId) {
        throw new UnsupportedOperationException("Implementation for findAncestors (UUID) not yet provided");
    }

    @Override
    public List<Category> findAncestors(String descendantPath) {
        return jpaRepository.findAncestorsByPath(descendantPath);
    }

    @Override
    public List<Category> findByLevel(int level) {
        return jpaRepository.findByLevel(level);
    }
}