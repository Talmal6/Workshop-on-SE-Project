package com.SEGroup.Domain;

import java.time.Instant;

public record GuestSession(String token, Instant createdAt , ShoppingCart shoppingCart) {
}
