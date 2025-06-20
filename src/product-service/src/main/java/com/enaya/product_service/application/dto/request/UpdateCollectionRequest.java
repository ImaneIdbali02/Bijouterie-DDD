package com.enaya.product_service.application.dto.request;

import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import com.enaya.product_service.domain.model.collection.valueobjects.PeriodCollection;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateCollectionRequest {
    @NotBlank(message = "Le nom de la collection est requis")
    private String name;

    @NotBlank(message = "La description de la collection est requise")
    private String description;

    @NotNull(message = "La période de la collection est requise")
    private PeriodCollection period;

    private List<@Valid ImageCollection> images;
    private List<UUID> productIds;
    private String metaTitle;
    private String metaDescription;
    private Integer priority;
    private Boolean active;
    private Boolean published;
} 