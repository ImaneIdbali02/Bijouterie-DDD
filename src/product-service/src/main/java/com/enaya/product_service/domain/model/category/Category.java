package com.enaya.product_service.domain.model.category;


import com.enaya.product_service.domain.model.category.valueobjects.MetadonneesCategorie;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Categorie {

    private UUID id;
    private String nom;
    private String description;
    private String slug;
    private UUID categorieParenteId;
    private List<UUID> categoriesEnfantIds;
    private int ordreAffichage;
    private String cheminComplet; // e.g., "Electronics > Computers > Laptops"
    private int niveau; // 0 for root categories, 1 for first level children, etc.
    private MetadonneesCategorie metadonnees;
    private boolean active;
    private boolean visibleMenu;
    private String imageUrl;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private int version;

    @Builder
    private Categorie(UUID id, String nom, String description, String slug,
                      UUID categorieParenteId, List<UUID> categoriesEnfantIds,
                      int ordreAffichage, String cheminComplet, int niveau,
                      MetadonneesCategorie metadonnees, boolean active,
                      boolean visibleMenu, String imageUrl) {
        this.id = id != null ? id : UUID.randomUUID();
        this.nom = validateNom(nom);
        this.description = description;
        this.slug = generateSlugIfNeeded(slug, nom);
        this.categorieParenteId = categorieParenteId;
        this.categoriesEnfantIds = categoriesEnfantIds != null ?
                new ArrayList<>(categoriesEnfantIds) : new ArrayList<>();
        this.ordreAffichage = Math.max(0, ordreAffichage);
        this.cheminComplet = cheminComplet;
        this.niveau = Math.max(0, niveau);
        this.metadonnees = metadonnees;
        this.active = active;
        this.visibleMenu = visibleMenu;
        this.imageUrl = imageUrl;
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.version = 0;
    }

    public static Categorie createRoot(String nom, String description) {
        return Categorie.builder()
                .nom(nom)
                .description(description)
                .niveau(0)
                .cheminComplet(nom)
                .active(true)
                .visibleMenu(true)
                .ordreAffichage(0)
                .build();
    }

    public static Categorie createChild(String nom, String description, UUID parentId,
                                        String parentPath, int parentLevel) {
        return Categorie.builder()
                .nom(nom)
                .description(description)
                .categorieParenteId(parentId)
                .niveau(parentLevel + 1)
                .cheminComplet(parentPath + " > " + nom)
                .active(true)
                .visibleMenu(true)
                .ordreAffichage(0)
                .build();
    }

    public void updateBasicInfo(String nom, String description) {
        this.nom = validateNom(nom);
        this.description = description;
        this.slug = generateSlugIfNeeded(null, nom);
        updateModificationDate();
    }

    public void updateMetadonnees(MetadonneesCategorie metadonnees) {
        this.metadonnees = metadonnees;
        updateModificationDate();
    }

    public void updateDisplayOrder(int newOrder) {
        this.ordreAffichage = Math.max(0, newOrder);
        updateModificationDate();
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
        updateModificationDate();
    }

    public void removeImage() {
        this.imageUrl = null;
        updateModificationDate();
    }

    public void activate() {
        this.active = true;
        updateModificationDate();
    }

    public void deactivate() {
        this.active = false;
        updateModificationDate();
    }

    public void showInMenu() {
        this.visibleMenu = true;
        updateModificationDate();
    }

    public void hideFromMenu() {
        this.visibleMenu = false;
        updateModificationDate();
    }

    public void addChildCategory(UUID childId) {
        if (childId != null && !this.categoriesEnfantIds.contains(childId)) {
            this.categoriesEnfantIds.add(childId);
            updateModificationDate();
        }
    }

    public void removeChildCategory(UUID childId) {
        if (this.categoriesEnfantIds.remove(childId)) {
            updateModificationDate();
        }
    }

    public void updateHierarchyInfo(String nouveauCheminComplet, int nouveauNiveau) {
        this.cheminComplet = nouveauCheminComplet;
        this.niveau = Math.max(0, nouveauNiveau);
        updateModificationDate();
    }

    public void moveToParent(UUID nouveauParentId, String nouveauCheminComplet, int nouveauNiveau) {
        this.categorieParenteId = nouveauParentId;
        this.cheminComplet = nouveauCheminComplet;
        this.niveau = Math.max(0, nouveauNiveau);
        updateModificationDate();
    }

    public boolean isRootCategory() {
        return this.categorieParenteId == null;
    }

    public boolean hasChildren() {
        return !this.categoriesEnfantIds.isEmpty();
    }

    public boolean hasParent() {
        return this.categorieParenteId != null;
    }

    public boolean isLeafCategory() {
        return this.categoriesEnfantIds.isEmpty();
    }

    public boolean canBeDeleted() {
        return this.categoriesEnfantIds.isEmpty();
    }

    public boolean isDescendantOf(UUID ancestorId) {
        if (ancestorId == null || this.cheminComplet == null) {
            return false;
        }
        // This is a simplified check - in practice you'd traverse the hierarchy
        return this.categorieParenteId != null && this.categorieParenteId.equals(ancestorId);
    }

    public int getMaxDepthFromHere() {
        // This would typically be calculated by traversing child categories
        // For now, return a simple calculation based on current level
        return Math.max(0, 10 - this.niveau); // Assuming max depth of 10
    }

    public List<String> getPathSegments() {
        if (this.cheminComplet == null) {
            return new ArrayList<>();
        }
        return List.of(this.cheminComplet.split(" > "));
    }

    public boolean hasImage() {
        return this.imageUrl != null && !this.imageUrl.trim().isEmpty();
    }

    public boolean hasMetadonnees() {
        return this.metadonnees != null;
    }

    private void updateModificationDate() {
        this.dateModification = LocalDateTime.now();
        this.version++;
    }

    private String validateNom(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        if (nom.length() > 255) {
            throw new IllegalArgumentException("Category name cannot exceed 255 characters");
        }
        return nom.trim();
    }

    private String generateSlugIfNeeded(String providedSlug, String nom) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return validateAndNormalizeSlug(providedSlug);
        }

        // Generate slug from name
        return nom.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-") // Replace spaces with hyphens
                .replaceAll("-+", "-") // Replace multiple hyphens with single
                .replaceAll("^-|-$", ""); // Remove leading/trailing hyphens
    }

    private String validateAndNormalizeSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            throw new IllegalArgumentException("Slug cannot be null or empty");
        }

        String normalizedSlug = slug.toLowerCase().trim();
        if (!normalizedSlug.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Slug must contain only lowercase letters, numbers, and hyphens");
        }

        if (normalizedSlug.startsWith("-") || normalizedSlug.endsWith("-")) {
            throw new IllegalArgumentException("Slug cannot start or end with a hyphen");
        }

        if (normalizedSlug.contains("--")) {
            throw new IllegalArgumentException("Slug cannot contain consecutive hyphens");
        }

        if (normalizedSlug.length() > 100) {
            throw new IllegalArgumentException("Slug cannot exceed 100 characters");
        }

        return normalizedSlug;
    }
}