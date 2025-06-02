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
import org.springframework.stereotype.Repository;


/**
 * TransactionRepository is responsible for managing transactions in the system.
 * It provides methods to add, retrieve, update, and delete transactions.
 */

import com.SEGroup.Infrastructure.Repositories.RepositoryData.TransactionData;
import org.springframework.context.annotation.Profile;

@Repository
@Profile({"prod", "db"})
public class TransactionRepository implements ITransactionRepository {

    private final TransactionData transactionData;

    public TransactionRepository(TransactionData transactionData) {
        this.transactionData = transactionData;
    }

    public TransactionRepository() {
        this.transactionData = new com.SEGroup.Infrastructure.Repositories.RepositoryData.InMemoryTransactionData();
    }

    @Override
    public void addTransaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName) {
        Transaction transaction = new Transaction(shoppingProductIds, cost, buyersEmail, storeName);
        transactionData.saveTransaction(transaction);
    }

    @Override
    public TransactionDTO getTransactionById(int id) {
        Transaction transaction = transactionData.getTransactionById(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found");
        }
        return TransactionMapper.toDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionData.getAllTransactions().stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }

    @Override
    public void updateTransaction(Transaction updated) {
        transactionData.updateTransaction(updated);
    }

    @Override
    public void deleteTransaction(int id) {
        transactionData.deleteTransaction(id);
    }

    @Override
    public List<TransactionDTO> getTransactionsByUserEmail(String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        return transactionData.getTransactionsByUserEmail(userEmail).stream()
                .map(TransactionMapper::toDTO)
                .toList();
    }
}
