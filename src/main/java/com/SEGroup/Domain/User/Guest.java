package com.SEGroup.Domain.User;

import java.time.Instant;

public record Guest(String id , Instant issuedAt, ShoppingCart cart ) {
}
