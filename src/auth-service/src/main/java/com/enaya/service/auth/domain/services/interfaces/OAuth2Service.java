package com.enaya.service.auth.domain.services.interfaces;

import com.enaya.service.auth.domain.valueobjects.OAuth2UserInfo;
import com.enaya.service.auth.exception.AuthenticationException;

public interface OAuth2Service {
    /**
     * Validate OAuth2 token and get user info
     * @param token The OAuth2 token from the provider
     * @param provider The provider (GOOGLE or FACEBOOK)
     * @return User information from the provider
     */
    OAuth2UserInfo validateToken(String token, String provider) throws AuthenticationException;
} 