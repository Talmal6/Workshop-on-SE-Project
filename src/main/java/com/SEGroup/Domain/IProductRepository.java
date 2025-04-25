package com.SEGroup.Domain;

import java.util.List;

public interface IProductRepository {

    List<ProductDTO> getAllProducts();

    List<ProductDTO> searchProducts(String query);

    List<ProductDTO> getProductsFromCategory(String category);

    void addNewProductToCatalog(String catalogID, String );

}
