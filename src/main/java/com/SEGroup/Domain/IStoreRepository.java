package com.SEGroup.Domain;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface IStoreRepository {
    List<StoreDTO> getAllStores();
    StoreDTO findByName(String name);
    boolean existsByName(String name);
    void addStore(StoreDTO store);
    void updateStore(StoreDTO store);
    void deleteStore(String storeId);
}