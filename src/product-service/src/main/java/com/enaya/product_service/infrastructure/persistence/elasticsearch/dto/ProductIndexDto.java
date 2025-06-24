package com.enaya.product_service.infrastructure.persistence.elasticsearch.dto;

import com.enaya.product_service.domain.model.product.Product;
import com.enaya.product_service.domain.model.product.ProductVariant;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;
import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
public class ProductIndexDto {
    private UUID id;
    private String name;
    private String description;
    private String sku;
    private PriceDto price;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime modificationDate;
    
    private UUID categoryId;
    private List<UUID> collectionIds;
    private boolean active;
    private long version;
    private List<AttributeDto> attributes;
    private List<ImageDto> images;
    private List<VariantDto> variants;

    @Data
    @Builder
    public static class PriceDto {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @Builder
    public static class AttributeDto {
        private String name;
        private String value;
        private String type;
    }

    @Data
    @Builder
    public static class ImageDto {
        private String url;
        private String altText;
        private int displayOrder;
        private String type;
        private boolean main;
    }

    @Data
    @Builder
    public static class VariantDto {
        private UUID id;
        private String name;
        private String sku;
        private PriceDto price;
        private boolean active;
        private String stockStatus;
        private Double rating;
        private Integer reviewCount;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime creationDate;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime modificationDate;
        
        private long version;
    }

    public static ProductIndexDto fromProduct(Product product) {
        return ProductIndexDto.builder()
                .id(product.getId())
                .name(product.getName() != null ? product.getName() : "")
                .description(product.getDescription() != null ? product.getDescription() : "")
                .sku(product.getSku() != null ? product.getSku() : "")
                .price(product.getPrice() != null ? PriceDto.builder()
                        .amount(product.getPrice().getAmount())
                        .currency(product.getPrice().getCurrency() != null ? product.getPrice().getCurrency().getCurrencyCode() : "USD")
                        .build() : null)
                .creationDate(product.getCreationDate())
                .modificationDate(product.getModificationDate())
                .categoryId(product.getCategoryId())
                .collectionIds(product.getCollections() != null ? product.getCollections().stream()
                        .map(collection -> collection != null ? collection.getId() : null)
                        .filter(id -> id != null)
                        .collect(Collectors.toList()) : List.of())
                .active(product.isActive())
                .version(product.getVersion())
                .attributes(product.getAttributes() != null ? product.getAttributes().stream()
                        .map(attr -> AttributeDto.builder()
                                .name(attr.getName() != null ? attr.getName() : "")
                                .value(attr.getValue() != null ? attr.getValue() : "")
                                .type(attr.getType() != null ? attr.getType().name() : "UNKNOWN")
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .images(product.getImages() != null ? product.getImages().stream()
                        .map(img -> ImageDto.builder()
                                .url(img.getUrl() != null ? img.getUrl() : "")
                                .altText(img.getAltText() != null ? img.getAltText() : "")
                                .displayOrder(img.getDisplayOrder())
                                .type(img.getType() != null ? img.getType().name() : "UNKNOWN")
                                .main(img.getDisplayOrder() == 0) // First image is main
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .variants(product.getVariants() != null ? product.getVariants().stream()
                        .map(variant -> VariantDto.builder()
                                .id(variant.getId())
                                .name(variant.getName() != null ? variant.getName() : "")
                                .sku(variant.getSku() != null ? variant.getSku() : "")
                                .price(variant.getPrice() != null ? PriceDto.builder()
                                        .amount(variant.getPrice().getAmount())
                                        .currency(variant.getPrice().getCurrency() != null ? variant.getPrice().getCurrency().getCurrencyCode() : "USD")
                                        .build() : null)
                                .active(variant.isActive())
                                .stockStatus(variant.getStockStatus() != null ? variant.getStockStatus().name() : "UNKNOWN")
                                .rating(variant.getRating())
                                .reviewCount(variant.getReviewCount())
                                .creationDate(variant.getCreationDate())
                                .modificationDate(variant.getModificationDate())
                                .version(variant.getVersion())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
} 