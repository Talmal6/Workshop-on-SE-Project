package com.SEGroup.Domain.ProductCatalog;

public class CatalogProduct {
    private final String catalogID; 
    private final String name; // Example: Iphone 13
    private final String brand; // Example: Apple
    private final String description; // IOS PHONE 

    public CatalogProduct(String catalogID, String name, String brand, String description) {
        this.catalogID = catalogID;
        this.name = name;
        this.brand = brand;
        this.description = description;
    }

    public String getCatalogID() { return catalogID; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    
}
