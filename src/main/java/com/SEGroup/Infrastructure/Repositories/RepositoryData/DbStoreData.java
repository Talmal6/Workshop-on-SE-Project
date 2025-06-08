package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.Store.Store;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaStoreRepository;
import java.util.List;
import java.util.function.Supplier;

public class DbStoreData implements StoreData {

    private final JpaStoreRepository jpaStoreRepository;

    public DbStoreData(JpaStoreRepository jpaStoreRepository) {
        this.jpaStoreRepository = jpaStoreRepository;
    }

    @Override
    public Store findByName(String storeName) {
        return DbSafeExecutor.safeExecute("findByName", () -> jpaStoreRepository.findByName(storeName));
    }

    @Override
    public void saveStore(Store store) {
        DbSafeExecutor.safeExecute("saveStore", () -> {
            jpaStoreRepository.save(store);
            return null;
        });
    }

    @Override
    public boolean isStoreExist(String storeName) {
        return DbSafeExecutor.safeExecute("isStoreExist", () -> jpaStoreRepository.existsById(storeName));
    }

    @Override
    public List<Store> getStoresOwnedBy(String ownerEmail) {
        return DbSafeExecutor.safeExecute("getStoresOwnedBy", () -> jpaStoreRepository.getStoresOwnedBy(ownerEmail));
    }

    @Override
    public void updateStore(Store store) {
        DbSafeExecutor.safeExecute("updateStore", () -> {
            jpaStoreRepository.save(store);
            return null;
        });
    }

    @Override
    public List<Store> getAllStores() {
        return DbSafeExecutor.safeExecute("getAllStores", jpaStoreRepository::findAll);
    }



}
