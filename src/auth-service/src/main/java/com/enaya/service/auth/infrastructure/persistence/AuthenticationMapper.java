package com.enaya.service.auth.infrastructure.persistence;

import com.enaya.service.auth.domain.aggregates.Authentication;


public class AuthenticationMapper {

    public static Authentication toDomain(AuthenticationEntity entity) {
        if (entity == null) return null;

        return Authentication.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .provider(entity.getProvider())
                .providerUserId(entity.getProviderUserId())
                .enabled(entity.isEnabled())
                .locked(entity.isLocked())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .role(entity.getRole())
                .build();
    }

    public static AuthenticationEntity toEntity(Authentication domain) {
        if (domain == null) return null;

        AuthenticationEntity entity = new AuthenticationEntity();
        entity.setId(domain.getId());
        entity.setClientId(domain.getClientId());
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setProvider(domain.getProvider());
        entity.setProviderUserId(domain.getProviderUserId());
        entity.setEnabled(domain.isEnabled());
        entity.setLocked(domain.isLocked());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        entity.setRole(domain.getRole());
        return entity;
    }
}
