package com.enaya.service.auth.domain.services.interfaces;

public interface PasswordHashingService {

    String hashPassword(String rawPassword);


    boolean verifyPassword(String rawPassword, String storedHash);
}
