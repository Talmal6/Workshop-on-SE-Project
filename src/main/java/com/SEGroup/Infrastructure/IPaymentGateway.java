package com.SEGroup.Infrastructure;

public class IPaymentGateway {
    public void processPayment(String paymentDetails , double amount) {
        // Process payment implementation
    }

    public boolean validatePayment(String paymentDetails) {
        // Validate payment implementation
        return true;
    }

    public String getPaymentStatus(String transactionId) {
        // Get payment status implementation
        return "Success";
    }
}
