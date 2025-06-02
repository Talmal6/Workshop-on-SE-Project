package com.SEGroup.Infrastructure.Repositories.JpaDatabase;

import com.SEGroup.Infrastructure.Repositories.DataBaseRepositories.CatalogProductEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
@Profile({"db", "prod"})
public interface JpaCatalogRepository
        extends JpaRepository<CatalogProductEntity, String> { }