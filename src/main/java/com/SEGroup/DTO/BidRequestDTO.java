package com.SEGroup.DTO;

public class BidRequestDTO {


    private String email;


    private double amount;


    public BidRequestDTO(String email, double amount){
        this.email = email;
        this.amount = amount;


    }





    public double getAmount() {
        return amount;
    }





    public String getEmail() {
        return email;
    }





    public void setAmount(double amount) {


        this.amount = amount;


    }





    public void setEmail(String email) {


        this.email = email;


    }


}