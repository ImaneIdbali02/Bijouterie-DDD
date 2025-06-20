package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.product.valueobjects.Price;
import com.enaya.product_service.domain.model.product.valueobjects.JewelryDimensions;
import com.enaya.product_service.domain.model.product.valueobjects.ProductAttribute;
import com.enaya.product_service.domain.model.product.valueobjects.ProductImage;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CreateProductVariantRequest {

    @NotBlank(message = "Le nom de la variante est obligatoire")
    @Size(max = 255, message = "Le nom de la variante ne peut pas dépasser 255 caractères")
    private String name;

    @NotBlank(message = "Le SKU de la variante est obligatoire")
    @Size(max = 100, message = "Le SKU de la variante ne peut pas dépasser 100 caractères")
    private String sku;

    @NotNull(message = "Le prix de la variante est obligatoire")
    private Price price;

    private JewelryDimensions dimensions;

    private List<ProductAttribute> specificAttributes;

    private List<ProductImage> images;

    private boolean active = true;

    private Integer stockQuantity;

    private Double rating;

    private Integer reviewCount = 0;
} 