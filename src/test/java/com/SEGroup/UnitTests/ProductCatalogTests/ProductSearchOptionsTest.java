package com.SEGroup.UnitTests.ProductCatalogTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.SEGroup.Domain.IProductCatalog.ProductSearchOptions;

public class ProductSearchOptionsTest {
    private ProductSearchOptions options;

    @BeforeEach
    void init() {
        options = new ProductSearchOptions();
    }

    @Test
    @DisplayName("Given valid values, when setting and getting fields, then correct values are returned")
    void GivenValidValues_WhenSettersCalled_ThenGettersReturnCorrectValues() {
        options.setText("iPhone");
        options.setCategory("Electronics");
        options.setMinPrice(500.0);
        options.setMaxPrice(1500.0);
        options.setBrand("Apple");

        assertEquals("iPhone", options.getText());
        assertEquals("Electronics", options.getCategory());
        assertEquals(500.0, options.getMinPrice());
        assertEquals(1500.0, options.getMaxPrice());
        assertEquals("Apple", options.getBrand());
    }

    @Test
    @DisplayName("Given unset fields, when getting fields, then null values are returned")
    void GivenUnsetFields_WhenGettersCalled_ThenReturnNull() {
        assertNull(options.getText());
        assertNull(options.getCategory());
        assertNull(options.getMinPrice());
        assertNull(options.getMaxPrice());
        assertNull(options.getBrand());
    }

    @Test
    @DisplayName("Given setting null values, when setting fields, then fields are set to null correctly")
    void GivenSettingNulls_WhenFieldsSet_ThenFieldsRemainNull() {
        options.setText(null);
        options.setCategory(null);
        options.setMinPrice(null);
        options.setMaxPrice(null);
        options.setBrand(null);

        assertNull(options.getText());
        assertNull(options.getCategory());
        assertNull(options.getMinPrice());
        assertNull(options.getMaxPrice());
        assertNull(options.getBrand());
    }
}
