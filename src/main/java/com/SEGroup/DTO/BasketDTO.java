package com.SEGroup.DTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record BasketDTO(String storeId,
                        Map<String,Integer> prod2qty) {

    public List<String> getBasketProducts() {
        List<String> out = new ArrayList<>();
        prod2qty.forEach((pid,q) -> out.addAll(java.util.Collections.nCopies(q, pid)));
        return out;
    }
}
