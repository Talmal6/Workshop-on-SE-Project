package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "catalog_products")
public class CatalogProductEntity {

    @Id
    @Column(name = "catalog_id")
    private String catalogId;

    private String name;
    private String brand;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "catalog_product_categories",
            joinColumns = @JoinColumn(name = "catalog_id")
    )
    @Column(name = "category")
    private List<String> categories = new ArrayList<>();

    // -----------------------------------------------------------------------------------
    // 1) No-arg constructor (required by JPA)
    // -----------------------------------------------------------------------------------
    public CatalogProductEntity() {
        // JPA needs this
    }

    // -----------------------------------------------------------------------------------
    // 2) All-args constructor (so that DbProductCatalog can do `new CatalogProductEntity(id, name, brand, description, categories)`)
    // -----------------------------------------------------------------------------------
    public CatalogProductEntity(String catalogId,
                                String name,
                                String brand,
                                String description,
                                List<String> categories) {
        this.catalogId = catalogId;
        this.name = name;
        this.brand = brand;
        this.description = description;
        // Copy into a fresh ArrayList to “initialize” the lazy collection immediately
        this.categories = (categories == null)
                ? new ArrayList<>()
                : new ArrayList<>(categories);
    }

    // -----------------------------------------------------------------------------------
    // 3) Public getters for every field (so code like `e.getCategories()` compiles)
    // -----------------------------------------------------------------------------------
    public String getCatalogId() {
        return catalogId;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCategories() {
        return categories;
    }

    // -----------------------------------------------------------------------------------
    // 4) toDomain() method—copies the (possibly‐lazy) List<String> into a new List<>,
    //    so that once the Hibernate session closes, we have a plain Java list to hand back.
    // -----------------------------------------------------------------------------------
    public CatalogProduct toDomain() {
        List<String> cats = (categories == null)
                ? Collections.emptyList()
                : new ArrayList<>(categories);

        return new CatalogProduct(
                this.catalogId,
                this.name,
                this.brand,
                this.description,
                cats
        );
    }
}
