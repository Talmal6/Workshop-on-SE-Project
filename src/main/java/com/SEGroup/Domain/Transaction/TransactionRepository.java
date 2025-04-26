package com.SEGroup.Domain.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.ITransactionRepository;
import com.SEGroup.Service.Mapper.TransactionMapper;




public class TransactionRepository implements ITransactionRepository {
    private final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>(); // <Identifier, Transaction>
    private final AtomicInteger nextId = new AtomicInteger(1); // Identifier for transactions
    public TransactionRepository() {
        // Constructor can be used for initialization if needed
    }
    
    @Override
    public void addTransaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName) {
        Transaction transaction = new Transaction(shoppingProductIds, cost, buyersEmail, storeName);
        transactions.put(nextId.incrementAndGet(), transaction);
    }

    @Override
    public TransactionDTO getTransactionById(int id) { 
        Transaction transaction = transactions.get(id);
        if (transaction == null) {
            throw new RuntimeException("Transaction not found");
        }
        return TransactionMapper.toDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getAllTransactions() {
        List<TransactionDTO> transactionDTOs = new ArrayList<>();
        for (Transaction transaction : transactions.values()) {
            transactionDTOs.add(TransactionMapper.toDTO(transaction));
        }
        return transactionDTOs;
    }

    @Override
    public void updateTransaction(Transaction updated) {
        for (Map.Entry<Integer, Transaction> entry : transactions.entrySet()) {
            if (entry.getValue().equals(updated)) {
                transactions.put(entry.getKey(), updated);
                return;
            }
        }
    }

    @Override
    public void deleteTransaction(int id) {
        if (!transactions.containsKey(id)) {
            throw new RuntimeException("Transaction not found");
        }
        transactions.remove(id);
    }

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