package com.SEGroup.Domain;

public class Transaction {
    private String userEmail;
    private double amount;

    // Constructor to match the usage in PaymentService
    public Transaction(String userEmail, double amount) {
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