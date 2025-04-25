package com.SEGroup.DTO;

import java.util.List;

public class StoreDTO {
    public int id;
    public String name;
    public String founderEmail;
    public boolean isActive;
    public double balance;
    public List<ShoppingProductDTO> products;

    public StoreDTO(int id, String name, String founderEmail, boolean isActive, double balance,
            List<ShoppingProductDTO> products) {
        this.id = id;
        this.name = name;
        this.founderEmail = founderEmail;
        this.isActive = isActive;
        this.balance = balance;
        this.products = products;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFounderEmail() {
        return founderEmail;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getBalance() {
        return balance;
    }

    public List<ShoppingProductDTO> getProducts() {
        return products;
    }
}
