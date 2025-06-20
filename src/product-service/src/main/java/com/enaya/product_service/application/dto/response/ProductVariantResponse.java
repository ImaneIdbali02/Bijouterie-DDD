package com.enaya.product_service.application.dto.response;

import com.enaya.product_service.domain.model.product.ProductVariant;
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
public class ProductVariantResponse {
    private UUID id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String currency;
    private Dimensions dimensions;
    private List<Attribute> specificAttributes;
    private List<Image> images;
    private boolean active;
    private ProductVariant.StockStatus stockStatus;
    private Double rating;
    private Integer reviewCount;
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimensions {
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
        private BigDecimal weight;
    }
}