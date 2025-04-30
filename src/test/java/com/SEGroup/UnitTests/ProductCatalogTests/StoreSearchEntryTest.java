package com.SEGroup.UnitTests.ProductCatalogTests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.SEGroup.Domain.IProductCatalog.StoreSearchEntry;

public class StoreSearchEntryTest {
    private StoreSearchEntry entry;

    @BeforeEach
    public void init() {
        entry = new StoreSearchEntry("catalogID", "storeName", "productID", 100.0, 10, 4.5, "name");
    }

    @Test
    @DisplayName("Given valid StoreSearchEntry, when getters are called, then correct values are returned")
    void GivenValidEntry_WhenGettersCalled_ThenCorrectValuesReturned() {
        assertEquals("catalogID", entry.getCatalogID());
        assertEquals("storeName", entry.getStoreName());
        assertEquals("productID", entry.getProductID());
        assertEquals(100.0, entry.getPrice());
        assertEquals(10, entry.getQuantity());
        assertEquals(4.5, entry.getRating());
    }

    @Test
    @DisplayName("Given updated values, when setters are called, then values are updated")
    void GivenUpdatedValues_WhenSettersCalled_ThenValuesUpdated() {
        entry.setPrice(150.0);
        entry.setQuantity(20);
        entry.setRating(5.0);

        assertEquals(150.0, entry.getPrice());
        assertEquals(20, entry.getQuantity());
        assertEquals(5.0, entry.getRating());
    }

    @Test
    @DisplayName("Given matching filters, when matchesQuery is called, then true is returned")
    void GivenMatchingFilters_WhenMatchesQueryCalled_ThenTrueReturned() {
        List<String> filters = Arrays.asList("price<200", "price>50", "rating>4", "quantity>5");
        assertTrue(entry.matchesQuery("product", filters));
    }

    @Test
    @DisplayName("Given non-matching price filter, when matchesQuery is called, then false is returned")
    void GivenNonMatchingPriceFilter_WhenMatchesQueryCalled_ThenFalseReturned() {
        List<String> filters = Collections.singletonList("price<50");
        assertFalse(entry.matchesQuery("product", filters));
    }

    @Test
    @DisplayName("Given non-matching rating filter, when matchesQuery is called, then false is returned")
    void GivenNonMatchingRatingFilter_WhenMatchesQueryCalled_ThenFalseReturned() {
        List<String> filters = Collections.singletonList("rating>5");
        assertFalse(entry.matchesQuery("product", filters));
    }

    @Test
    @DisplayName("Given non-matching quantity filter, when matchesQuery is called, then false is returned")
    void GivenNonMatchingQuantityFilter_WhenMatchesQueryCalled_ThenFalseReturned() {
        List<String> filters = Collections.singletonList("quantity<5");
        assertFalse(entry.matchesQuery("product", filters));
    }

    @Test
    @DisplayName("Given empty filters, when matchesQuery is called, then true is returned")
    void GivenEmptyFilters_WhenMatchesQueryCalled_ThenTrueReturned() {
        List<String> filters = Collections.emptyList();
        assertTrue(entry.matchesQuery("product", filters));
    }
}
