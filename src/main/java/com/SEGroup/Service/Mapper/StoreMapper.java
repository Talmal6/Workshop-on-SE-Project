package com.SEGroup.Service.Mapper;

//hey
import java.util.ArrayList;

import java.util.List;


import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;

public class StoreMapper {

    public StoreDTO toDTO(Store domainInstance) {
        List<ShoppingProductDTO> productDTOs = new ArrayList<>();

        for (ShoppingProduct product : domainInstance.getAllProducts()) {
            productDTOs.add(new ShoppingProductDTO(
                    product.getStoreName(),
                    product.getCategory(),
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getQuantity(),
                    product.averageRating()
            ));
        }
        return new StoreDTO(domainInstance.getId(),
                domainInstance.getName(),
                domainInstance.getfounderEmail(),
                domainInstance.isActive(),
                domainInstance.getBalance(),
                productDTOs
                        ,domainInstance.averageRating());
    }

    public List<StoreDTO> toDTOs(List<Store> domainInstances) {
        List<StoreDTO> storeDTOs = new ArrayList<>();
        for (Store store : domainInstances) {
            storeDTOs.add(toDTO(store));
        }
        return storeDTOs;
    }

}
