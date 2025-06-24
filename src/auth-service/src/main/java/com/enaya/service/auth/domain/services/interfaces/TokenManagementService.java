package com.enaya.service.auth.domain.services.interfaces;

import com.enaya.service.auth.domain.aggregates.Authentication;
import com.enaya.service.auth.exception.AuthenticationException;

import java.util.UUID;


public interface TokenManagementService {

    String generateAccessToken(Authentication authentication);

    String generateRefreshToken(Authentication authentication);


    Authentication validateRefreshToken(String refreshToken) throws AuthenticationException;


    boolean verifyAccessToken(String accessToken);


    UUID invalidateAccessToken(String accessToken);


    void invalidateAllTokens(UUID authenticationId);


    long getAccessTokenExpiryTime();
}
