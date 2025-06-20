package com.enaya.product_service.domain.model.category.valueobjects;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MetadonneesCategorie {

    private String titreSeo;
    private String descriptionSeo;
    private List<String> motsCles;
    private String metaRobots; // index, noindex, follow, nofollow
    private String canonicalUrl;
    private String schemaMarkup;
    private String openGraphTitle;
    private String openGraphDescription;
    private String openGraphImage;
    private String twitterTitle;
    private String twitterDescription;
    private String twitterImage;

    private MetadonneesCategorie(String titreSeo, String descriptionSeo, List<String> motsCles,
                                 String metaRobots, String canonicalUrl, String schemaMarkup,
                                 String openGraphTitle, String openGraphDescription, String openGraphImage,
                                 String twitterTitle, String twitterDescription, String twitterImage) {
        this.titreSeo = validateTitreSeo(titreSeo);
        this.descriptionSeo = validateDescriptionSeo(descriptionSeo);
        this.motsCles = motsCles != null ? new ArrayList<>(motsCles) : new ArrayList<>();
        this.metaRobots = validateMetaRobots(metaRobots);
        this.canonicalUrl = canonicalUrl;
        this.schemaMarkup = schemaMarkup;
        this.openGraphTitle = validateOpenGraphTitle(openGraphTitle);
        this.openGraphDescription = validateOpenGraphDescription(openGraphDescription);
        this.openGraphImage = openGraphImage;
        this.twitterTitle = validateTwitterTitle(twitterTitle);
        this.twitterDescription = validateTwitterDescription(twitterDescription);
        this.twitterImage = twitterImage;
    }

    public static MetadonneesCategorie basic(String titreSeo, String descriptionSeo) {
        return new MetadonneesCategorie(titreSeo, descriptionSeo, null, "index,follow",
                null, null, null, null, null, null, null, null);
    }

    public static MetadonneesCategorie complete(String titreSeo, String descriptionSeo,
                                                List<String> motsCles, String metaRobots) {
        return new MetadonneesCategorie(titreSeo, descriptionSeo, motsCles, metaRobots,
                null, null, null, null, null, null, null, null);
    }

    public static MetadonneesCategorie withSocialMedia(String titreSeo, String descriptionSeo,
                                                       List<String> motsCles, String openGraphTitle,
                                                       String openGraphDescription, String openGraphImage,
                                                       String twitterTitle, String twitterDescription,
                                                       String twitterImage) {
        return new MetadonneesCategorie(titreSeo, descriptionSeo, motsCles, "index,follow",
                null, null, openGraphTitle, openGraphDescription, openGraphImage,
                twitterTitle, twitterDescription, twitterImage);
    }

    public static Builder builder() {
        return new Builder();
    }

    public MetadonneesCategorie addMotCle(String motCle) {
        if (motCle != null && !motCle.trim().isEmpty() && !this.motsCles.contains(motCle.trim())) {
            List<String> nouveauxMotsCles = new ArrayList<>(this.motsCles);
            nouveauxMotsCles.add(motCle.trim());
            return new MetadonneesCategorie(this.titreSeo, this.descriptionSeo, nouveauxMotsCles,
                    this.metaRobots, this.canonicalUrl, this.schemaMarkup,
                    this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                    this.twitterTitle, this.twitterDescription, this.twitterImage);
        }
        return this;
    }

    public MetadonneesCategorie removeMotCle(String motCle) {
        if (motCle != null && this.motsCles.contains(motCle.trim())) {
            List<String> nouveauxMotsCles = new ArrayList<>(this.motsCles);
            nouveauxMotsCles.remove(motCle.trim());
            return new MetadonneesCategorie(this.titreSeo, this.descriptionSeo, nouveauxMotsCles,
                    this.metaRobots, this.canonicalUrl, this.schemaMarkup,
                    this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                    this.twitterTitle, this.twitterDescription, this.twitterImage);
        }
        return this;
    }

    public MetadonneesCategorie updateCanonicalUrl(String canonicalUrl) {
        return new MetadonneesCategorie(this.titreSeo, this.descriptionSeo, this.motsCles,
                this.metaRobots, canonicalUrl, this.schemaMarkup,
                this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                this.twitterTitle, this.twitterDescription, this.twitterImage);
    }

    public MetadonneesCategorie updateSchemaMarkup(String schemaMarkup) {
        return new MetadonneesCategorie(this.titreSeo, this.descriptionSeo, this.motsCles,
                this.metaRobots, this.canonicalUrl, schemaMarkup,
                this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                this.twitterTitle, this.twitterDescription, this.twitterImage);
    }

    public String getMotsClesAsString() {
        return String.join(", ", this.motsCles);
    }

    public boolean hasMotsCles() {
        return !this.motsCles.isEmpty();
    }

    public boolean isIndexable() {
        return this.metaRobots != null && this.metaRobots.contains("index");
    }

    public boolean isFollowable() {
        return this.metaRobots != null && this.metaRobots.contains("follow");
    }

    public boolean hasOpenGraphData() {
        return this.openGraphTitle != null || this.openGraphDescription != null || this.openGraphImage != null;
    }

    public boolean hasTwitterData() {
        return this.twitterTitle != null || this.twitterDescription != null || this.twitterImage != null;
    }

    public boolean hasCanonicalUrl() {
        return this.canonicalUrl != null && !this.canonicalUrl.trim().isEmpty();
    }

    public boolean hasSchemaMarkup() {
        return this.schemaMarkup != null && !this.schemaMarkup.trim().isEmpty();
    }

    private String validateTitreSeo(String titre) {
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("SEO title cannot be null or empty");
        }
        if (titre.length() > 60) {
            throw new IllegalArgumentException("SEO title should not exceed 60 characters for optimal display");
        }
        return titre.trim();
    }

    private String validateDescriptionSeo(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("SEO description cannot be null or empty");
        }
        if (description.length() > 160) {
            throw new IllegalArgumentException("SEO description should not exceed 160 characters for optimal display");
        }
        return description.trim();
    }

    private String validateMetaRobots(String metaRobots) {
        if (metaRobots == null || metaRobots.trim().isEmpty()) {
            return "index,follow"; // Default value
        }

        String normalized = metaRobots.toLowerCase().trim();
        if (!normalized.matches("^(index|noindex),(follow|nofollow)$")) {
            throw new IllegalArgumentException("Meta robots must be in format 'index,follow' or similar valid combinations");
        }
        return normalized;
    }

    private String validateOpenGraphTitle(String title) {
        if (title != null && title.length() > 95) {
            throw new IllegalArgumentException("Open Graph title should not exceed 95 characters");
        }
        return title != null ? title.trim() : null;
    }

    private String validateOpenGraphDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("Open Graph description should not exceed 200 characters");
        }
        return description != null ? description.trim() : null;
    }

    private String validateTwitterTitle(String title) {
        if (title != null && title.length() > 70) {
            throw new IllegalArgumentException("Twitter title should not exceed 70 characters");
        }
        return title != null ? title.trim() : null;
    }

    private String validateTwitterDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new IllegalArgumentException("Twitter description should not exceed 200 characters");
        }
        return description != null ? description.trim() : null;
    }

    public static class Builder {
        private String titreSeo;
        private String descriptionSeo;
        private List<String> motsCles = new ArrayList<>();
        private String metaRobots = "index,follow";
        private String canonicalUrl;
        private String schemaMarkup;
        private String openGraphTitle;
        private String openGraphDescription;
        private String openGraphImage;
        private String twitterTitle;
        private String twitterDescription;
        private String twitterImage;

        public Builder titreSeo(String titreSeo) {
            this.titreSeo = titreSeo;
            return this;
        }

        public Builder descriptionSeo(String descriptionSeo) {
            this.descriptionSeo = descriptionSeo;
            return this;
        }

        public Builder motsCles(List<String> motsCles) {
            this.motsCles = motsCles != null ? new ArrayList<>(motsCles) : new ArrayList<>();
            return this;
        }

        public Builder addMotCle(String motCle) {
            if (motCle != null && !motCle.trim().isEmpty()) {
                this.motsCles.add(motCle.trim());
            }
            return this;
        }

        public Builder metaRobots(String metaRobots) {
            this.metaRobots = metaRobots;
            return this;
        }

        public Builder canonicalUrl(String canonicalUrl) {
            this.canonicalUrl = canonicalUrl;
            return this;
        }

        public Builder schemaMarkup(String schemaMarkup) {
            this.schemaMarkup = schemaMarkup;
            return this;
        }

        public Builder openGraph(String title, String description, String image) {
            this.openGraphTitle = title;
            this.openGraphDescription = description;
            this.openGraphImage = image;
            return this;
        }

        public Builder twitter(String title, String description, String image) {
            this.twitterTitle = title;
            this.twitterDescription = description;
            this.twitterImage = image;
            return this;
        }

        public MetadonneesCategorie build() {
            return new MetadonneesCategorie(titreSeo, descriptionSeo, motsCles, metaRobots,
                    canonicalUrl, schemaMarkup, openGraphTitle,
                    openGraphDescription, openGraphImage, twitterTitle,
                    twitterDescription, twitterImage);
        }
    }
}