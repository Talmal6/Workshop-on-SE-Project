package com.SEGroup.Service.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.SEGroup.DTO.ShoppingProductDTO;
import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.ShoppingProduct;
import com.SEGroup.Domain.Store.Store;

public class StoreMapper {

    public StoreDTO toDTO(Store domainInstance){
        List<ShoppingProductDTO> productDTOs = new ArrayList<>();

        for (ShoppingProduct product : domainInstance.getAllProducts()) {
            productDTOs.add(new ShoppingProductDTO(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity()
            ));
        }
        return new StoreDTO(domainInstance.getId(),
            domainInstance.getName(),
            domainInstance.getOwnerEmail(),
            domainInstance.isActive(),
            domainInstance.getBalance(),
            productDTOs);
    }

    public List<StoreDTO> toDTO(List<Store> domainInstances){
        List<StoreDTO> storeDTOs = new ArrayList<>();
        for (Store domainInstance : domainInstances){
            storeDTOs.add(toDTO(domainInstance));
        }
    }
}
