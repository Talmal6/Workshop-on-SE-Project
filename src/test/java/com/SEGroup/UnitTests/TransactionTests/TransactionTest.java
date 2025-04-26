package com.SEGroup.UnitTests.TransactionTests;
import com.SEGroup.Domain.Transaction.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {
    private Transaction transaction;
    private List<String> sampleItems;
    private double sampleCost;
    private String sampleEmail;
    private String sampleStore;

    @BeforeEach
    void setUp() {
        sampleItems = Arrays.asList("item1", "item2", "item3");
        sampleCost = 49.99;
        sampleEmail = "buyer@example.com";
        sampleStore = "Test Store";
        transaction = new Transaction(sampleItems, sampleCost, sampleEmail, sampleStore);
    }

    @Test
    @DisplayName("Given valid constructor arguments, when constructing a Transaction, then all fields should be initialized")
    void Given_ValidConstructorArgs_When_ConstructingTransaction_Then_AllFieldsAreInitialized() {
        assertEquals(sampleItems, transaction.getItemsToTransact());
        assertEquals(sampleCost, transaction.getCost());
        assertEquals(sampleEmail, transaction.getBuyersEmail());
        assertEquals(sampleStore, transaction.getStoreName());
    }

    @Test
    @DisplayName("Given an existing Transaction, when getItemsToTransact is called, then it should return the items list")
    void Given_ExistingTransaction_When_GetItemsToTransact_Then_ReturnItemsList() {
        assertEquals(sampleItems, transaction.getItemsToTransact());
    }

    @Test
    @DisplayName("Given an existing Transaction with items, when setItemsToTransact is called, then the items list should be updated")
    void Given_ExistingTransactionWithItems_When_SetItemsToTransact_Then_ItemsListIsUpdated() {
        List<String> newItems = Collections.singletonList("newItem");
        transaction.setItemsToTransact(newItems);
        assertEquals(newItems, transaction.getItemsToTransact());
    }

    @Test
    @DisplayName("Given an existing Transaction, when getCost is called, then it should return the cost")
    void Given_ExistingTransaction_When_GetCost_Then_ReturnCost() {
        assertEquals(sampleCost, transaction.getCost());
    }

    @Test
    @DisplayName("Given an existing Transaction with cost, when setCost is called, then the cost should be updated")
    void Given_ExistingTransactionWithCost_When_SetCost_Then_CostIsUpdated() {
        double newCost = 99.99;
        transaction.setCost(newCost);
        assertEquals(newCost, transaction.getCost());
    }

    @Test
    @DisplayName("Given an existing Transaction, when getBuyersEmail is called, then it should return the buyer's email")
    void Given_ExistingTransaction_When_GetBuyersEmail_Then_ReturnEmail() {
        assertEquals(sampleEmail, transaction.getBuyersEmail());
    }

    @Test
    @DisplayName("Given an existing Transaction with email, when setBuyersEmail is called, then the buyer's email should be updated")
    void Given_ExistingTransactionWithEmail_When_SetBuyersEmail_Then_EmailIsUpdated() {
        String newEmail = "newbuyer@example.com";
        transaction.setBuyersEmail(newEmail);
        assertEquals(newEmail, transaction.getBuyersEmail());
    }

    @Test
    @DisplayName("Given an existing Transaction, when getStoreName is called, then it should return the store name")
    void Given_ExistingTransaction_When_GetStoreName_Then_ReturnStoreName() {
        assertEquals(sampleStore, transaction.getStoreName());
    }

    @Test
    @DisplayName("Given an existing Transaction with store name, when setStoreName is called, then the store name should be updated")
    void Given_ExistingTransactionWithStoreName_When_SetStoreName_Then_StoreNameIsUpdated() {
        String newStore = "New Store Name";
        transaction.setStoreName(newStore);
        assertEquals(newStore, transaction.getStoreName());
    }
}
