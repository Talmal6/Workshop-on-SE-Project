package com.SEGroup.Domain.ProductCatalog;

import java.util.UUID;

public class Product {
    private final UUID productId;
    private final String name; // Example: Iphone 13
    private final String brand; // Example: Apple
    private final String description;

    public Product(UUID productId, String name, String brand, String description) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.description = description;
    }

    public UUID getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
}
