package com.SEGroup.Service.Mapper;

import com.SEGroup.Domain.User.Basket;
import com.SEGroup.DTO.BasketDTO;

public final class BasketMapper {

       // utility class


    public static BasketDTO toDTO(String storeId, Basket basket) {

        return new BasketDTO(storeId, basket.snapshot());


    }
}
