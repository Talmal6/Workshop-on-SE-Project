package com.SEGroup.Mapper;

import com.SEGroup.DTO.BasketDTO;
import com.SEGroup.Domain.User.Basket;

/**
 * Utility class for mapping a Basket object to a BasketDTO.
 * Provides a method to convert a Basket domain object to a corresponding BasketDTO.
 */
public final class BasketMapper {

    // Private constructor to prevent instantiation of this utility class
    private BasketMapper() {}

    /**
     * Converts a Basket domain object to a BasketDTO.
     *
     * @param storeId The ID of the store associated with the basket.
     * @param basket The Basket domain object to be converted.
     * @return A BasketDTO representing the same data as the Basket.
     */
    public static BasketDTO toDTO(String storeId, Basket basket) {
        // Convert the Basket object to a BasketDTO
        return new BasketDTO(storeId, basket.snapshot());
    }
}
