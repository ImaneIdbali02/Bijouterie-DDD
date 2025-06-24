package com.enaya.service.auth.domain.services.imp;

import com.enaya.service.auth.domain.services.interfaces.PasswordHashingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PasswordHashingServiceImpl implements PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String storedHash) {
        return passwordEncoder.matches(rawPassword, storedHash);
    }
}