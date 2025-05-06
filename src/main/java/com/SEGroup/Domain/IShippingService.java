package com.SEGroup.Domain;

import com.SEGroup.DTO.BasketDTO;

public interface IShippingService {
    /**
     * Processes shipping.
     *
     * @return true if the shipping operation was successful; false otherwise.
     */
    String ship(BasketDTO basket , String userId) throws Exception;
    String cancelShipping(BasketDTO basket,String userId);
}