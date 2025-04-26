package com.SEGroup.DTO;

public class ShoppingProductDTO {
    private String productId;
    private String catalogID;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String category;
    private String storeName;
    private double avgRating;

    public ShoppingProductDTO(String storeName, String catalogID,String productId, String name,String description, double price, int quantity,double avgRating) {
        this.productId = productId;
        this.catalogID = catalogID;
        this.name = name;
        this.description= description;
        this.price = price;
        this.quantity = quantity;
        this.storeName = storeName;
        this.avgRating = avgRating;
    }
    public String getCatalogID() {
        return catalogID;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    public double getAvgRating() {
        return avgRating;
    }

}
