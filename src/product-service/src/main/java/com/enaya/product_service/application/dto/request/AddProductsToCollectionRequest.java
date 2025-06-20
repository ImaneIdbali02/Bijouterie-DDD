package com.enaya.product_service.application.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class AddProductsToCollectionRequest {
    @NotNull(message = "L'ID de la collection est requis")
    private UUID collectionId;

    @NotEmpty(message = "La liste des produits ne peut pas être vide")
    private List<UUID> productIds;
} 