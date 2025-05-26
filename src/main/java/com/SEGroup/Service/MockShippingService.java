package com.SEGroup.Service;

import com.SEGroup.DTO.AddressDTO;
import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.IShippingService;
import org.springframework.stereotype.Service;


@Service
public class MockShippingService implements IShippingService {
//    @Override
//    public String ship(BasketDTO basket, String userEmail) {
//        // Mock implementation
//       return "Shipping products from " + basket.storeId() + " to " + userEmail;
//    }
//
//    @Override
//    public String cancelShipping(BasketDTO basket, String userEmail) {
//        // Mock implementation
//        return "Cancelling shipping from " + basket.storeId() + " to " + userEmail;
//    }
//
//    @Override
//    public String ship(String storeName, String productId, String userId) throws Exception {
//        return null;
//    }
    @Override
    public Integer ship(AddressDTO address_detail, String name){
        return 0;
    }

    @Override
    public Boolean cancelShipping(int shippingId){
        return false;
    }
}