package com.enaya.product_service.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CollectionProductsResponse {
    private UUID collectionId;
    private String collectionName;
    private List<ProductResponse> products;
    private int totalProducts;
    private boolean hasMoreProducts;
} 