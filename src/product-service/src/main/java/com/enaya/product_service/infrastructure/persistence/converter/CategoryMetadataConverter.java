package com.enaya.product_service.infrastructure.persistence.converter;

import com.enaya.product_service.domain.model.category.valueobjects.CategoryMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryMetadataConverter implements AttributeConverter<CategoryMetadata, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CategoryMetadata attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting CategoryMetadata to JSON", e);
        }
    }

    @Override
    public CategoryMetadata convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, CategoryMetadata.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to CategoryMetadata", e);
        }
    }
} 