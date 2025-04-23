package com.SEGroup.domain;

import com.SEGroup.Domain.PasswordPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordPolicyTest {

    @Test void accepts_strong_password() {
        assertThat(PasswordPolicy.isStrong("Abcdef1!")).isTrue();
    }

    @Test void rejects_weak_passwords() {
        assertThat(PasswordPolicy.isStrong("short1!")).isFalse();      // < 8
        assertThat(PasswordPolicy.isStrong("alllowercase1!")).isFalse();
        assertThat(PasswordPolicy.isStrong("ALLUPPERCASE1!")).isFalse();
        assertThat(PasswordPolicy.isStrong("NoDigits!")).isFalse();
        assertThat(PasswordPolicy.isStrong("NoSpecial1")).isFalse();
    }
}