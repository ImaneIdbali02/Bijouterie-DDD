package com.enaya.product_service.domain.repository;

import com.enaya.product_service.domain.model.category.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAll();
    List<Category> findActiveCategories();
    List<Category> findByParentId(UUID parentId);
    List<Category> findRootCategories();
    List<Category> findLeafCategories();
    List<Category> findVisibleInMenu();
    List<Category> findByNameContaining(String name);
    List<Category> findByPathContaining(String path);
    void delete(UUID id);
    boolean existsById(UUID id);
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    List<Category> findDescendants(UUID ancestorId);
    List<Category> findAncestors(UUID descendantId);

    List<Category> findAncestors(String descendantPath);

    List<Category> findByLevel(int level);
}
