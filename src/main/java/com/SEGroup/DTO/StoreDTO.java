package com.SEGroup.DTO;

import java.util.List;

public class StoreDTO {
    public int id;
    public String name;
    public String ownerEmail;
    public boolean isActive;
    public double balance;
    public List<ShoppingProductDTO> products;

    public StoreDTO(int id, String name, String ownerEmail, boolean isActive, double balance, List<ShoppingProductDTO> products) {
        this.id = id;
        this.name = name;
        this.ownerEmail = ownerEmail;
        this.isActive = isActive;
        this.balance = balance;
        this.products = products;
    }

    public StoreDTO(){}
}
