package com.SEGroup.Infrastructure.Repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.Mapper.TransactionMapper;



/**
 * TransactionRepository is responsible for managing transactions in the system.
 * It provides methods to add, retrieve, update, and delete transactions.
 */
public class TransactionRepository implements ITransactionRepository {
    private final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>(); // <Identifier, Transaction>
    private final AtomicInteger nextId = new AtomicInteger(1); // Identifier for transactions
    public TransactionRepository() {
        // Constructor can be used for initialization if needed
    }

    /**
     * Adds a new transaction to the repository.
     *
     * @param shoppingProductIds List of product IDs involved in the transaction
     * @param cost              Total cost of the transaction
     * @param buyersEmail       Email of the buyer
     * @param storeName         Name of the store where the transaction occurred
     */
    @Override
    public void addTransaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName) {
        Transaction transaction = new Transaction(shoppingProductIds, cost, buyersEmail, storeName);
        transactions.put(nextId.incrementAndGet(), transaction);
    }

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction to retrieve
     * @return The TransactionDTO object representing the transaction
     */
    @Override
    public TransactionDTO getTransactionById(int id) { 
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found");
        }
        return TransactionMapper.toDTO(transaction);
    }

    /**
     * Retrieves all transactions in the repository.
     *
     * @return A list of TransactionDTO objects representing all transactions
     */
    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for (Transaction transaction : transactions.values()) {
            transactionDTOs.add(TransactionMapper.toDTO(transaction));
        }
        return transactionDTOs;
    }

    /**
     * Updates an existing transaction in the repository.
     *
     * @param updated The updated Transaction object
     */
    @Override
    public void updateTransaction(Transaction updated) {
        for (Map.Entry<Integer, Transaction> entry : transactions.entrySet()) {
            if (entry.getValue().equals(updated)) {
                transactions.put(entry.getKey(), updated);
                return;
            }
        }
    }

    /**
     * Deletes a transaction by its ID.
     *
     * @param id The ID of the transaction to delete
     */
    @Override
    public void deleteTransaction(int id) {
        if (!transactions.containsKey(id)) {
            throw new RuntimeException("Transaction not found");
        }
        transactions.remove(id);
    }

    /**
     * Retrieves all transactions associated with a specific user email.
     *
     * @param userEmail The email of the user whose transactions to retrieve
     * @return A list of TransactionDTO objects representing the user's transactions
     */
    @Override
    public List<TransactionDTO> getTransactionsByUserEmail(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (transactions.isEmpty()) {
            throw new RuntimeException("No transactions found for the user");
        }
        List<TransactionDTO> result = new ArrayList<>();
        for (Transaction t : transactions.values()) {
            if (t.getBuyersEmail().equalsIgnoreCase(userEmail)) {
                result.add(TransactionMapper.toDTO(t));
            }
        }
        return result;
    }
}