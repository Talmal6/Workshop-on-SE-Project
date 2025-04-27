package com.SEGroup.Domain;

import java.util.List;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.Transaction.Transaction;

/**
 * Interface representing a repository for managing transactions.
 * It provides methods for adding, retrieving, updating, and deleting transactions.
 */
public interface ITransactionRepository {

    /**
     * Adds a new transaction to the repository.
     *
     * @param shoppingProductIds A list of product IDs involved in the transaction.
     * @param cost The total cost of the transaction.
     * @param buyersEmail The email of the buyer making the transaction.
     * @param storeName The name of the store where the transaction occurred.
     */
    void addTransaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName);

    /**
     * Retrieves a transaction by its ID.
     *
     * @param id The ID of the transaction.
     * @return A TransactionDTO object containing the details of the transaction.
     */
    TransactionDTO getTransactionById(int id);

    /**
     * Retrieves all transactions from the repository.
     *
     * @return A list of all TransactionDTO objects.
     */
    List<TransactionDTO> getAllTransactions();

    /**
     * Updates an existing transaction in the repository.
     *
     * @param transaction The Transaction object containing the updated details.
     */
    void updateTransaction(Transaction transaction);

    /**
     * Deletes a transaction from the repository by its ID.
     *
     * @param id The ID of the transaction to delete.
     */
    void deleteTransaction(int id);

    /**
     * Retrieves all transactions associated with a specific user based on their email.
     *
     * @param userEmail The email address of the user.
     * @return A list of TransactionDTO objects related to the specified user.
     */
    List<TransactionDTO> getTransactionsByUserEmail(String userEmail);
}
