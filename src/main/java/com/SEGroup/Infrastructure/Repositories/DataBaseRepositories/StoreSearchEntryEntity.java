package com.SEGroup.Infrastructure.Repositories.DataBaseRepositories;

import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;
import jakarta.persistence.*;

@Entity
@Table(name = "store_products")
public class StoreSearchEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String catalogId;
    private String storeName;
    private String productId;
    private double price;
    private int    quantity;
    private double rating;
    private String name;
    private String imageUrl;

    protected StoreSearchEntryEntity() {}

    public StoreSearchEntryEntity(
            String catalogId,
            String storeName,
            String productId,
            double price,
            int    quantity,
            double rating,
            String name,
            String imageUrl) {

        this.catalogId  = catalogId;
        this.storeName  = storeName;
        this.productId  = productId;
        this.price      = price;
        this.quantity   = quantity;
        this.rating     = rating;
        this.name       = name;
        this.imageUrl   = imageUrl;
    }

    /* ---------- getters / setters Spring needs ---------- */
    public String getCatalogId() { return catalogId; }
    public String getStoreName() { return storeName; }
    public String getProductId() { return productId; }

    public void setPrice(double price)     { this.price = price; }
    public void setQuantity(int quantity)  { this.quantity = quantity; }
    public void setRating(double rating)   { this.rating  = rating; }

    /* ---------- domain ↔ entity adapters ---------- */
    public StoreSearchEntry toDomain() {
        StoreSearchEntry e = new StoreSearchEntry(
                catalogId, storeName, productId, price, quantity, rating, name);
        e.setImageUrl(imageUrl);
        return e;
    }
}
