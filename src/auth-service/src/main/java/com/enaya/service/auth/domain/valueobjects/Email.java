package com.enaya.service.auth.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.regex.Pattern;


@Getter
@EqualsAndHashCode
public final class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private final String value;

    private Email(String value) {
        if (!isValidEmail(value)) {
            throw new IllegalArgumentException("Email invalide : " + value);
        }
        this.value = value.toLowerCase(); // juste pour la normalisation
    }

    public static Email of(String value) {
        return new Email(value);
    }

    private static boolean isValidEmail(String value) {
        return value != null && EMAIL_PATTERN.matcher(value).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
