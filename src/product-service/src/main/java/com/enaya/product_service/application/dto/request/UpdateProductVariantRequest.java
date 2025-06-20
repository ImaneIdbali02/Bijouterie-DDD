package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.product.valueobjects.JewelryDimensions;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;
import com.enaya.product_service.domain.model.product.ProductVariant;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateProductVariantRequest {

    private UUID id;

    @Size(max = 255, message = "Le nom de la variante ne peut pas dépasser 255 caractères")
    private String name;

    private Price price;

    private JewelryDimensions dimensions;

    private List<ProductAttribute> specificAttributes;

    private List<ProductImage> images;

    private Boolean active;

    private ProductVariant.StockStatus stockStatus;

    private Integer stockQuantity;

    private Double rating;

    private Integer reviewCount;
} 