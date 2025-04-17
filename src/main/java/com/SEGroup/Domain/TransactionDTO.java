package com.SEGroup.Domain;

public class TransactionDTO {
    private String userEmail;
    private double amount;

    // Constructor to match the usage in PaymentService
    public TransactionDTO(String userEmail, double amount) {
        this.userEmail = userEmail;
        this.amount = amount;
    }

    // Getters and setters (if needed)
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}