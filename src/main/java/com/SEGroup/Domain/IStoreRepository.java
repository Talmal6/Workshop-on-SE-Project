package com.SEGroup.Domain;
import java.util.List;

public interface IStoreRepository {
    // Define methods for store-related operations
    void addStore(StoreDTO store);
    StoreDTO getStoreById(int id);
    void updateStore(StoreDTO store);
    void deleteStore(int id);
    List<StoreDTO> getAllStores();
    StoreDTO findByName(String name);
    boolean existsByName(String name);
}
