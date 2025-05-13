package com.SEGroup.DTO;

public class BidDTO {
    private String bidderEmail;
    private String product;
    private double price;
    private int quantity;

    public BidDTO(String bidderEmail, String product, double amount, int quantity) {
        if (bidderEmail == null || bidderEmail.isEmpty())
            throw new IllegalArgumentException("Bidder email cannot be null or empty");
        if (product == null || product.isEmpty())
            throw new IllegalArgumentException("Product cannot be null or empty");
        if (amount <= 0)
            throw new IllegalArgumentException("Bid amount must be positive");
        if (quantity <= 0)
            throw new IllegalArgumentException("Bid quantity must be positive");

        this.bidderEmail = bidderEmail;
        this.product = product;
        this.price = amount;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public String getProductId() {
        return product;
    }

    public double getPrice() {
        return price;
    }
    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "Bid{email='" + bidderEmail + "', product='" + product + "', amount=" + price + '}';
    }
}
