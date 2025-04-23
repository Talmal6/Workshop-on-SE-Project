package com.SEGroup.Domain;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Basket {
    private final String storeId;
    private final Map<String, Integer> product2qty = new ConcurrentHashMap<>();

    Basket(String storeId) {

        this.storeId = storeId;

    }


    void add(String pid, int q) {
        product2qty.merge(pid, q, Integer::sum);  // im adding more to quantitiy of pid

    }
    void change(String pid, int q) {

        product2qty.put(pid, q);

    }
    public Map<String,Integer> snapshot() {  // to see the current Basket list

        return Collections.unmodifiableMap(product2qty);

    }
    public String storeId() {

        return storeId;
    }
}
