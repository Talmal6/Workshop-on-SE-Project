package com.SEGroup.Domain;

import java.util.regex.Pattern;

public final class PasswordPolicy {

    private static final Pattern PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    );

    // ≥8 chars, 1 upper, 1 lower, 1 digit, 1 special

    private PasswordPolicy() { }

    public static boolean isStrong(String raw) {
        return PATTERN.matcher(raw).matches();
    }
}