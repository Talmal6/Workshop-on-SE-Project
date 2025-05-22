package com.SEGroup.UnitTests.ProductCatalogTests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.SEGroup.Domain.ProductCatalog.CatalogProduct;

import java.util.List;


public class CatalogProductTests {

    static CatalogProduct catalogProduct1;
    static CatalogProduct catalogProduct2;
    
    @BeforeAll
    static void init(){
        catalogProduct1 = new CatalogProduct("catalog123", "Nike Air Force", "Nike", "Comfortable shoes", List.of());
        catalogProduct2 = new CatalogProduct(null, null, null, null,null);
    }

    @Test
    void GivenCatalogProduct_WhenGetName_ThenSucceeds(){
        assertNotEquals(null, catalogProduct1.getName());
    }

    @Test
    void GivenCatalogProduct_WhenGetCatalogID_ThenSucceeds(){
        assertNotEquals(null, catalogProduct1.getCatalogID());
    }

    @Test
    void GivenCatalogProduct_WhenGetBrand_ThenSucceeds(){
        assertNotEquals(null, catalogProduct1.getBrand());
    }

    @Test
    void GivenCatalogProduct_WhenGetDescription_ThenSucceeds(){
        assertNotEquals(null, catalogProduct1.getDescription());
    }

    @Test
    void GivenCatalogProductWithNullName_WhenGetName_ThenFails(){
        assertEquals(null, catalogProduct2.getName());
    }

    @Test
    void GivenCatalogProductWithNullCatalogID_WhenGetCatalogID_ThenFails(){
        assertEquals(null, catalogProduct2.getCatalogID());
    }

    @Test
    void GivenCatalogProductWithNullBrand_WhenGetBrand_ThenFails(){
        assertEquals(null, catalogProduct2.getBrand());
    }

    @Test
    void GivenCatalogProductWithNullDescription_WhenGetDescription_ThenFails(){
        assertEquals(null, catalogProduct2.getDescription());
    }
}
