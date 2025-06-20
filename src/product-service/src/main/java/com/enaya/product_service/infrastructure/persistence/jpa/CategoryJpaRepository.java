package com.enaya.product_service.infrastructure.persistence.jpa;

import com.enaya.product_service.domain.model.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {

    @Query("SELECT c FROM Category c WHERE c.active = true")
    List<Category> findActiveCategories();

    @Query("SELECT c FROM Category c WHERE c.parentId = :parentId")
    List<Category> findByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT c FROM Category c WHERE c.parentId IS NULL")
    List<Category> findRootCategories();

    @Query(value = "SELECT * FROM categories WHERE child_category_ids IS NULL OR array_length(child_category_ids, 1) IS NULL", nativeQuery = true)
    List<Category> findLeafCategories();

    @Query("SELECT c FROM Category c WHERE c.visibleInMenu = true")
    List<Category> findVisibleInMenu();

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContaining(@Param("name") String name);

    @Query("SELECT c FROM Category c WHERE c.fullPath LIKE CONCAT('%', :path, '%')")
    List<Category> findByPathContaining(@Param("path") String path);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.slug = :slug")
    boolean existsBySlug(@Param("slug") String slug);

    @Query("SELECT c FROM Category c JOIN Category ancestor ON c.fullPath LIKE CONCAT(ancestor.fullPath, '%') WHERE ancestor.id = :ancestorId")
    List<Category> findDescendants(@Param("ancestorId") UUID ancestorId);

    @Query("SELECT c FROM Category c WHERE :descendantPath LIKE CONCAT(c.fullPath, '%')")
    List<Category> findAncestorsByPath(@Param("descendantPath") String descendantPath);

    @Query("SELECT c FROM Category c WHERE c.level = :level")
    List<Category> findByLevel(@Param("level") int level);

    Optional<Category> findByPath(String path);
    Optional<Category> findBySlug(String slug);
} 