package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;
import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateProductRequest {
    @Size(max = 255, message = "Le nom du produit ne peut pas dépasser 255 caractères")
    private String name;

    @Size(max = 1000, message = "La description du produit ne peut pas dépasser 1000 caractères")
    private String description;

    private Price price;

    private UUID categoryId;

    private List<UUID> collectionIds;

    private List<UpdateProductVariantRequest> variants;

    private List<ProductAttribute> attributes;

    private List<ProductImage> images;

    private Boolean active;
} 