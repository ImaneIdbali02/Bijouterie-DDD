package com.enaya.product_service.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private String currency;
    private UUID categoryId;
    private List<UUID> collectionIds;
    private List<ProductVariantResponse> variants;
    private List<Attribute> attributes;
    private List<Image> images;
    private boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attribute {
        private String name;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String url;
        private String altText;
        private int displayOrder;
    }
}
