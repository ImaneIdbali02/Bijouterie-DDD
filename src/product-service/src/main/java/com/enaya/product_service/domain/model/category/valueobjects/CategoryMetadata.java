package com.enaya.product_service.domain.model.category.valueobjects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CategoryMetadata {

    @Column(name = "seo_title")
    String seoTitle;
    
    @Column(name = "seo_description")
    String seoDescription;
    
    @Column(name = "keywords")
    List<String> keywords;
    
    @Column(name = "meta_robots")
    String metaRobots; // index, noindex, follow, nofollow
    
    @Column(name = "canonical_url")
    String canonicalUrl;
    
    @Column(name = "schema_markup")
    String schemaMarkup;
    
    @Column(name = "open_graph_title")
    String openGraphTitle;
    
    @Column(name = "open_graph_description")
    String openGraphDescription;
    
    @Column(name = "open_graph_image")
    String openGraphImage;
    
    @Column(name = "twitter_title")
    String twitterTitle;
    
    @Column(name = "twitter_description")
    String twitterDescription;
    
    @Column(name = "twitter_image")
    String twitterImage;

    private CategoryMetadata(String seoTitle, String seoDescription, List<String> keywords,
                             String metaRobots, String canonicalUrl, String schemaMarkup,
                             String openGraphTitle, String openGraphDescription, String openGraphImage,
                             String twitterTitle, String twitterDescription, String twitterImage) {
        this.seoTitle = validateSeoTitle(seoTitle);
        this.seoDescription = validateSeoDescription(seoDescription);
        this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
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

    public static CategoryMetadata basic(String seoTitle, String seoDescription) {
        return new CategoryMetadata(seoTitle, seoDescription, null, "index,follow",
                null, null, null, null, null, null, null, null);
    }

    public static CategoryMetadata complete(String seoTitle, String seoDescription,
                                            List<String> keywords, String metaRobots) {
        return new CategoryMetadata(seoTitle, seoDescription, keywords, metaRobots,
                null, null, null, null, null, null, null, null);
    }

    public static CategoryMetadata withSocialMedia(String seoTitle, String seoDescription,
                                                   List<String> keywords, String openGraphTitle,
                                                   String openGraphDescription, String openGraphImage,
                                                   String twitterTitle, String twitterDescription,
                                                   String twitterImage) {
        return new CategoryMetadata(seoTitle, seoDescription, keywords, "index,follow",
                null, null, openGraphTitle, openGraphDescription, openGraphImage,
                twitterTitle, twitterDescription, twitterImage);
    }

    public static Builder builder() {
        return new Builder();
    }

    public CategoryMetadata addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty() && !this.keywords.contains(keyword.trim())) {
            List<String> newKeywords = new ArrayList<>(this.keywords);
            newKeywords.add(keyword.trim());
            return new CategoryMetadata(this.seoTitle, this.seoDescription, newKeywords,
                    this.metaRobots, this.canonicalUrl, this.schemaMarkup,
                    this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                    this.twitterTitle, this.twitterDescription, this.twitterImage);
        }
        return this;
    }

    public CategoryMetadata removeKeyword(String keyword) {
        if (keyword != null && this.keywords.contains(keyword.trim())) {
            List<String> newKeywords = new ArrayList<>(this.keywords);
            newKeywords.remove(keyword.trim());
            return new CategoryMetadata(this.seoTitle, this.seoDescription, newKeywords,
                    this.metaRobots, this.canonicalUrl, this.schemaMarkup,
                    this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                    this.twitterTitle, this.twitterDescription, this.twitterImage);
        }
        return this;
    }

    public CategoryMetadata updateCanonicalUrl(String canonicalUrl) {
        return new CategoryMetadata(this.seoTitle, this.seoDescription, this.keywords,
                this.metaRobots, canonicalUrl, this.schemaMarkup,
                this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                this.twitterTitle, this.twitterDescription, this.twitterImage);
    }

    public CategoryMetadata updateSchemaMarkup(String schemaMarkup) {
        return new CategoryMetadata(this.seoTitle, this.seoDescription, this.keywords,
                this.metaRobots, this.canonicalUrl, schemaMarkup,
                this.openGraphTitle, this.openGraphDescription, this.openGraphImage,
                this.twitterTitle, this.twitterDescription, this.twitterImage);
    }

    public String getKeywordsAsString() {
        return String.join(", ", this.keywords);
    }

    public boolean hasKeywords() {
        return !this.keywords.isEmpty();
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

    private String validateSeoTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("SEO title cannot be null or empty");
        }
        if (title.length() > 60) {
            throw new IllegalArgumentException("SEO title should not exceed 60 characters for optimal display");
        }
        return title.trim();
    }

    private String validateSeoDescription(String description) {
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
        private String seoTitle;
        private String seoDescription;
        private List<String> keywords = new ArrayList<>();
        private String metaRobots = "index,follow";
        private String canonicalUrl;
        private String schemaMarkup;
        private String openGraphTitle;
        private String openGraphDescription;
        private String openGraphImage;
        private String twitterTitle;
        private String twitterDescription;
        private String twitterImage;

        public Builder seoTitle(String seoTitle) {
            this.seoTitle = seoTitle;
            return this;
        }

        public Builder seoDescription(String seoDescription) {
            this.seoDescription = seoDescription;
            return this;
        }

        public Builder keywords(List<String> keywords) {
            this.keywords = keywords != null ? new ArrayList<>(keywords) : new ArrayList<>();
            return this;
        }

        public Builder addKeyword(String keyword) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                this.keywords.add(keyword.trim());
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

        public CategoryMetadata build() {
            return new CategoryMetadata(seoTitle, seoDescription, keywords, metaRobots,
                    canonicalUrl, schemaMarkup, openGraphTitle,
                    openGraphDescription, openGraphImage, twitterTitle,
                    twitterDescription, twitterImage);
        }
    }
}
