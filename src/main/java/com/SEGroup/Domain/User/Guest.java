package com.SEGroup.Domain.User;

import java.time.Instant;

/**
 * Represents a guest user in the system, including their ID, the time the guest was created,
 * and their shopping cart.
 */
public record Guest(String id , Instant issuedAt, ShoppingCart cart ) {
}
