package com.SEGroup.Domain;

public class Product {
    private String name;
    private double price;
    private final String storeName;

    public Product(String name, double price, String storeName) {
        this.name = name;
        this.price = price;
        this.storeName = storeName;
    }

    public Product(String name, String storeName, double price) {
        this.name = name;
        this.storeName = storeName;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getStoreName() {
        return storeName;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setProduct(Product other) {
        this.name = other.name;
        this.price = other.price;
    }
    

    
}
