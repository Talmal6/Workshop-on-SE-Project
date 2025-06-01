package com.SEGroup.Infrastructure;

import com.SEGroup.Domain.IPaymentGateway;
import org.springframework.stereotype.Component;

public class MockPaymentGateway implements IPaymentGateway{

    @Override
    public void processPayment(String paymentDetails, double amount) {
        System.out.println("Just a mock!");
    }

    @Override
    public boolean validatePayment(String paymentDetails) {
        return true;
    }

    @Override
    public String getPaymentStatus(String transactionId) {
        return "Success!";
    }
    
}
