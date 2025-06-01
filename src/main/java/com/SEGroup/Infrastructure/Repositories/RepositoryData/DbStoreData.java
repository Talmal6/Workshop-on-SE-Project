package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.DTO.StoreDTO;
import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaStoreRepository;
import java.util.List;

public class DbStoreData implements StoreData {

    private final JpaStoreRepository jpaStoreRepository;

    public DbStoreData(JpaStoreRepository jpaStoreRepository) {
        this.jpaStoreRepository = jpaStoreRepository;
    }


    @Override
    public Store findByName(String storeName) {
        return jpaStoreRepository.findByName(storeName);
    }

    @Override
    public void saveStore(Store store) {
        jpaStoreRepository.save(store);
    }
    @Override
    public boolean isStoreExist(String storeName) {
        return jpaStoreRepository.existsById(storeName);
    }
    @Override
    public List<Store> getStoresOwnedBy(String ownerEmail) {
        return jpaStoreRepository.getStoresOwnedBy(ownerEmail);
    }
    @Override
    public void updateStore(Store store) {
        jpaStoreRepository.save(store);
    }


    @Override
    public List<Store> getAllStores() {
        return jpaStoreRepository.findAll();
    }



}
