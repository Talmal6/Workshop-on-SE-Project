package com.SEGroup.Infrastructure.Repositories.JpaDatabase;

import com.SEGroup.Domain.Store.Store;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"db", "prod"})
public interface JpaStoreRepository extends JpaRepository<Store, String> {
    Store findByName(String name);
    boolean existsByName(String name);
    @Query
    List<Store>  getStoresOwnedBy(String OwnerEmail);

}