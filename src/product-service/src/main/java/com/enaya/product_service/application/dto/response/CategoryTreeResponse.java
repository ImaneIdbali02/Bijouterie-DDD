package com.enaya.product_service.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryTreeResponse {
    private CategoryResponse category;
    private List<CategoryTreeResponse> children;
    private int totalProducts;
    private boolean hasChildren;
} 