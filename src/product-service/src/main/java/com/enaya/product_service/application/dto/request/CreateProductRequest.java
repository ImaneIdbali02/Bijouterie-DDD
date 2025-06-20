package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 255, message = "Le nom du produit ne peut pas dépasser 255 caractères")
    private String name;

    @NotBlank(message = "La description du produit est obligatoire")
    @Size(max = 1000, message = "La description du produit ne peut pas dépasser 1000 caractères")
    private String description;

    @NotBlank(message = "Le SKU du produit est obligatoire")
    @Size(max = 100, message = "Le SKU du produit ne peut pas dépasser 100 caractères")
    private String sku;

    @NotNull(message = "Le prix du produit est obligatoire")
    private Price price;

    @NotNull(message = "L'ID de la catégorie est obligatoire")
    private UUID categoryId;

    private List<UUID> collectionIds;

    private List<CreateProductVariantRequest> variants;

    private List<ProductAttribute> attributes;

    private List<ProductImage> images;

    private boolean active = true;
} 