package com.SEGroup.Mapper;

import java.util.ArrayList;
import java.util.List;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;

/**
 * A mapper class for converting Store domain objects to StoreDTO data transfer objects.
 * Provides methods to map a single Store object or a list of Store objects to StoreDTOs.
 */
public class StoreMapper {

    /**
     * Converts a single Store domain object to a StoreDTO.
     *
     * @param domainInstance The Store domain object to convert.
     * @return A StoreDTO representing the Store domain object.
     */
    public StoreDTO toDTO(Store domainInstance) {
        // Create a list to hold the converted product DTOs
        List<ShoppingProductDTO> productDTOs = new ArrayList<>();

        // Convert each product from the store into a ShoppingProductDTO
        for (ShoppingProduct product : domainInstance.getAllProducts()) {
            productDTOs.add(new ShoppingProductDTO(
                    product.getStoreName(),
                    product.getCatalogID(),
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getQuantity(),
                    product.averageRating(),
                    product.getImageUrl(),
                    product.getCategories()
            ));
        }

        // Convert the Store domain object to a StoreDTO
        StoreDTO storeDTO = new StoreDTO(
                domainInstance.getId(),
                domainInstance.getName(),
                domainInstance.getfounderEmail(),
                domainInstance.isActive(),
                domainInstance.getBalance(),
                productDTOs,
                domainInstance.averageRating(),
                domainInstance.getDescription()
        );

        return storeDTO;
    }

    /**
     * Converts a list of Store domain objects to a list of StoreDTOs.
     *
     * @param domainInstances The list of Store domain objects to convert.
     * @return A list of StoreDTOs representing the given list of Store domain objects.
     */
    public List<StoreDTO> toDTOs(List<Store> domainInstances) {
        // Create a list to hold the converted StoreDTOs
        List<StoreDTO> storeDTOs = new ArrayList<>();

        // Convert each Store in the list to a StoreDTO
        for (Store store : domainInstances) {
            storeDTOs.add(toDTO(store));
        }

        return storeDTOs;
    }

}
