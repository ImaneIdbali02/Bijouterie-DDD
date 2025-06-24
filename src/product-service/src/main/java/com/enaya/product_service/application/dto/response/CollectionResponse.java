package com.enaya.product_service.application.dto.response;

import com.enaya.product_service.domain.model.collection.valueobjects.ImageCollection;
import com.enaya.product_service.domain.model.collection.valueobjects.PeriodCollection;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CollectionResponse {
    private UUID id;
    private String name;
    private String description;
    private String slug;
    private PeriodCollection period;
    private List<ImageCollection> images;
    private List<UUID> productIds;
    private String metaTitle;
    private String metaDescription;
    private int priority;
    private boolean active;
    private boolean published;
    private boolean archived;
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private long version;
} 