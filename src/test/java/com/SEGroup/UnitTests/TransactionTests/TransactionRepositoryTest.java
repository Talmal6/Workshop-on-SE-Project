package com.SEGroup.UnitTests.TransactionTests;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Infrastructure.Repositories.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionRepositoryTest {
    private TransactionRepository repo;

    @BeforeEach
    public void setUp() {
        repo = new TransactionRepository();
    }

    @Test
    @DisplayName("Given empty repo, when addTransaction is called, then getAllTransactions returns one DTO with correct fields")
    public void Given_EmptyRepo_When_AddTransaction_Then_ReturnsCorrectDTO() {
        List<String> items = Arrays.asList("p1", "p2");
        double cost = 20.5;
        String email = "user@test.com";
        String store = "TestStore";

        repo.addTransaction(items, cost, email, store);

        List<TransactionDTO> all = repo.getAllTransactions();
        assertEquals(1, all.size());
        TransactionDTO dto = all.get(0);
        assertEquals(items, dto.getItemsToTransact());
        assertEquals(cost, dto.getCost());
        assertEquals(email, dto.getBuyersEmail());
        assertEquals(store, dto.getSellerStore());
    }

    @Test
    @DisplayName("Given repo with one transaction, when getTransactionById with invalid ID, then RuntimeException is thrown")
    public void Given_RepoWithOneTransaction_When_GetByInvalidId_Then_ThrowsRuntimeException() {
        repo.addTransaction(Collections.emptyList(), 0, "e@e.com", "S");
        assertThrows(RuntimeException.class, () -> repo.getTransactionById(999));
    }

    @Test
    @DisplayName("Given repo with one transaction, when getTransactionById with valid ID, then DTO fields match")
    public void Given_RepoWithOneTransaction_When_GetByValidId_Then_ReturnsCorrectDTO() {
        repo.addTransaction(Collections.singletonList("x"), 5.0, "a@b.com", "StoreX");
        TransactionDTO dto = repo.getTransactionById(1);
        assertEquals(Collections.singletonList("x"), dto.getItemsToTransact());
        assertEquals(5.0, dto.getCost());
        assertEquals("a@b.com", dto.getBuyersEmail());
        assertEquals("StoreX", dto.getSellerStore());
    }

    @Test
    @DisplayName("Given repo with multiple transactions, when getAllTransactions is called, then returns all DTOs")
    public void Given_RepoWithMultipleTransactions_When_GetAllTransactions_Then_ReturnsAllDTOs() {
        repo.addTransaction(Arrays.asList("a"), 1, "u1", "S1");
        repo.addTransaction(Arrays.asList("b"), 2, "u2", "S2");

        List<TransactionDTO> all = repo.getAllTransactions();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Given repo with one transaction, when deleteTransaction with invalid ID, then RuntimeException is thrown")
    public void Given_RepoWithOneTransaction_When_DeleteInvalidId_Then_ThrowsRuntimeException() {
        repo.addTransaction(Collections.emptyList(), 0, "e@e.com", "S");
        assertThrows(RuntimeException.class, () -> repo.deleteTransaction(999));
    }

    @Test
    @DisplayName("Given repo with one transaction, when deleteTransaction with valid ID, then transaction is removed")
    public void Given_RepoWithOneTransaction_When_DeleteValidId_Then_TransactionIsRemoved() {
        repo.addTransaction(Collections.emptyList(), 0, "e@e.com", "S");
        repo.deleteTransaction(1);
        assertTrue(repo.getAllTransactions().isEmpty());
    }

    @Test
    @DisplayName("Given repo with transactions, when getTransactionsByUserEmail with null, then IllegalArgumentException is thrown")
    public void Given_RepoWithTransactions_When_GetByUserEmailNull_Then_ThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> repo.getTransactionsByUserEmail(null));
    }

    @Test
    @DisplayName("Given empty repo, when getTransactionsByUserEmail is called, then RuntimeException is thrown")
    public void Given_EmptyRepo_When_GetByUserEmail_Then_ThrowsRuntimeException() {
        assertThrows(RuntimeException.class, () -> repo.getTransactionsByUserEmail(""));
    }

    @Test
    @DisplayName("Given repo with mixed emails, when getTransactionsByUserEmail is called, then returns only matching DTOs case-insensitive")
    public void Given_RepoWithMixedEmails_When_GetByUserEmail_Then_ReturnsOnlyMatchingDTOs() {
        repo.addTransaction(Collections.emptyList(), 1, "match@e.com", "S");
        repo.addTransaction(Collections.emptyList(), 2, "other@e.com", "S");
        repo.addTransaction(Collections.emptyList(), 3, "MATCH@E.COM", "S");

        List<TransactionDTO> results = repo.getTransactionsByUserEmail("match@e.com");
        assertEquals(2, results.size());
        for (TransactionDTO dto : results) {
            assertEquals("match@e.com", dto.getBuyersEmail().toLowerCase());
        }
    }
}
