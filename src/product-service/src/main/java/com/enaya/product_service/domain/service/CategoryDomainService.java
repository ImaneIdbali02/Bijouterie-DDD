package com.enaya.product_service.domain.service;

import com.enaya.product_service.domain.model.category.Category;
import com.enaya.product_service.domain.model.category.valueobjects.CategoryMetadata;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryDomainService {

    /**
     * Crée une catégorie racine
     */
    public Category createRootCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        return Category.createRoot(name, description);
    }

    /**
     * Crée une catégorie enfant
     */
    public Category createChildCategory(String name, String description, UUID parentId,
                                      String parentPath, int parentLevel) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (parentId == null) {
            throw new IllegalArgumentException("Parent ID cannot be null");
        }
        return Category.createChild(name, description, parentId, parentPath, parentLevel);
    }

    /**
     * Met à jour les informations de base d'une catégorie
     */
    public void updateCategoryBasicInfo(Category category, String name, String description) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.updateBasicInfo(name, description);
    }

    /**
     * Met à jour les métadonnées d'une catégorie
     */


    /**
     * Met à jour l'ordre d'affichage d'une catégorie
     */
    public void updateCategoryDisplayOrder(Category category, int newOrder) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.updateDisplayOrder(newOrder);
    }

    /**
     * Définit l'image d'une catégorie
     */
    public void setCategoryImage(Category category, String imageUrl) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.setImage(imageUrl);
    }

    /**
     * Supprime l'image d'une catégorie
     */
    public void removeCategoryImage(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.removeImage();
    }

    /**
     * Active une catégorie
     */
    public void activateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.activate();
    }

    /**
     * Désactive une catégorie
     */
    public void deactivateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.deactivate();
    }

    /**
     * Rend une catégorie visible dans le menu
     */
    public void showCategoryInMenu(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.showInMenu();
    }

    /**
     * Cache une catégorie du menu
     */
    public void hideCategoryFromMenu(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.hideFromMenu();
    }

    /**
     * Ajoute une catégorie enfant
     */
    public void addChildCategory(Category parent, UUID childId) {
        if (parent == null || childId == null) {
            throw new IllegalArgumentException("Parent category and child ID cannot be null");
        }
        parent.addChildCategory(childId);
    }

    /**
     * Supprime une catégorie enfant
     */
    public void removeChildCategory(Category parent, UUID childId) {
        if (parent == null || childId == null) {
            throw new IllegalArgumentException("Parent category and child ID cannot be null");
        }
        parent.removeChildCategory(childId);
    }

    /**
     * Met à jour les informations de hiérarchie d'une catégorie
     */
    public void updateCategoryHierarchy(Category category, String newFullPath, int newLevel) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.updateHierarchyInfo(newFullPath, newLevel);
    }

    /**
     * Déplace une catégorie vers un nouveau parent
     */
    public void moveCategoryToParent(Category category, UUID newParentId, String newFullPath, int newLevel) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        category.moveToParent(newParentId, newFullPath, newLevel);
    }

    /**
     * Vérifie si une catégorie peut être supprimée
     */
    public boolean canDeleteCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.canBeDeleted();
    }

    /**
     * Vérifie si une catégorie est un descendant d'une autre
     */
    public boolean isDescendantOf(Category category, UUID ancestorId) {
        if (category == null || ancestorId == null) {
            return false;
        }
        return category.isDescendantOf(ancestorId);
    }

    /**
     * Calcule la profondeur maximale à partir d'une catégorie
     */
    public int calculateMaxDepth(Category category) {
        if (category == null) {
            return 0;
        }
        return category.getMaxDepthFromHere();
    }

    /**
     * Obtient les segments du chemin d'une catégorie
     */
    public List<String> getCategoryPathSegments(Category category) {
        if (category == null) {
            return new ArrayList<>();
        }
        return category.getPathSegments();
    }

    /**
     * Vérifie si une catégorie a une image
     */
    public boolean hasCategoryImage(Category category) {
        if (category == null) {
            return false;
        }
        return category.hasImage();
    }

    /**
     * Vérifie si une catégorie a des métadonnées
     */
    public boolean hasCategoryMetadata(Category category) {
        if (category == null) {
            return false;
        }
        return category.hasMetadata();
    }

    /**
     * Vérifie si une catégorie est une catégorie racine
     */
    public boolean isRootCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.isRootCategory();
    }

    /**
     * Vérifie si une catégorie a des enfants
     */
    public boolean hasChildCategories(Category category) {
        if (category == null) {
            return false;
        }
        return category.hasChildren();
    }

    /**
     * Vérifie si une catégorie a un parent
     */
    public boolean hasParentCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.hasParent();
    }

    /**
     * Vérifie si une catégorie est une feuille (n'a pas d'enfants)
     */
    public boolean isLeafCategory(Category category) {
        if (category == null) {
            return false;
        }
        return category.isLeafCategory();
    }
}
