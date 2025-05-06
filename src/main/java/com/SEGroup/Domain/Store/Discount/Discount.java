package com.SEGroup.Domain.Store.Discount;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;
import com.SEGroup.Domain.ProductCatalog.StoreSearchEntry;

import java.util.Map;


interface Discount {

    /**
     * * Calculates the discount amount to be applied
     * @param entries           An array of products in the basket (from a specific store).
     * @param catalogMap        A map of catalogID to CatalogProduct, used to retrieve brand/category information.
     * @return                  The total discount amount.
     **/
    double calculate(StoreSearchEntry[] entries, Map<String, CatalogProduct> catalogMap);

    /**
     * Returns a description of the discount.
     *
     * @return      A string describing the discount logic or conditions.
     */
    String getDescription();

}

