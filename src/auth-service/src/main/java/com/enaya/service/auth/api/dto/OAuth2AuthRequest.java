package com.enaya.service.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2AuthRequest {
    private String provider; // GOOGLE or FACEBOOK
    private String token; // Provider's token
}