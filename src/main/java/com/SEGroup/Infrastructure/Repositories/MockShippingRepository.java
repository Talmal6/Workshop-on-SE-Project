package com.SEGroup.Infrastructure.Repositories;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.IShippingService;
import org.springframework.stereotype.Repository;

@Repository
public class MockShippingRepository implements IShippingService {
    @Override
    public String ship(BasketDTO basket, String userId) throws Exception {
        return "success!";
    }

    @Override
    public String cancelShipping(BasketDTO basket, String userId) {
        return "shipping cancelled successfully!";
    }
}
