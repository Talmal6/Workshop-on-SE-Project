package com.SEGroup.Infrastructure.Repositories.JpaDatabase;

import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.StoreSearchEntryEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for StoreSearchEntryEntity.
 * Only active in “db” or “prod” profiles.
 */
@Repository
@Profile({"db","prod"})
public interface JpaStoreEntryRepository
        extends JpaRepository<StoreSearchEntryEntity, Long> {

    void deleteByCatalogIdAndStoreNameAndProductId(
            String catalogId, String storeName, String productId);

    List<StoreSearchEntryEntity> findByCatalogId(String catalogId);

    List<StoreSearchEntryEntity> findByStoreName(String storeName);
}
