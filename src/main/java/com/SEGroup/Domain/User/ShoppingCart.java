package com.SEGroup.Domain.User;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShoppingCart {



    private final Map<String, Basket> storeToBasket=new ConcurrentHashMap<>();
    public void add(String storeId, String productId, int qty) {
        storeToBasket
                .computeIfAbsent(storeId, Basket::new)
                .add(productId, qty);
    }

    public void changeQty(String storeId, String productId, int qty) {
        Basket basket = storeToBasket.get(storeId);
        if (basket ==null) throw new IllegalArgumentException("basket not found");
        basket.change(productId, qty);
    }



    public Map<String, Basket> snapShot(){
        return Collections.unmodifiableMap(storeToBasket);


    }
}
