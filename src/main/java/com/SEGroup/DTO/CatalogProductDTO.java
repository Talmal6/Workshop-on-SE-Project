package com.SEGroup.DTO;

import java.util.List;

public class CatalogProductDTO {
    private final String catalogId;
    private final String name;
    private final List<String> categories;

    public CatalogProductDTO(String catalogId, String name, List<String> categories) {
        this.catalogId  = catalogId;
        this.name       = name;
        this.categories = categories;
    }

    public String getCatalogId() { return catalogId; }
    public String getName()      { return name; }
    public List<String> getCategories() { return categories; }


}
