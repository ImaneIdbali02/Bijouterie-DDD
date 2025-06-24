package com.enaya.product_service.application.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class MoveCategoryRequest {
    @NotNull(message = "L'ID de la catégorie parente est requis")
    private UUID newParentId;
} 