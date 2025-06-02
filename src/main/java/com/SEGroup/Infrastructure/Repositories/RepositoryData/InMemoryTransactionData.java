package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.Transaction.Transaction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.List;

public class InMemoryTransactionData implements TransactionData {

    private final Map<Integer, Transaction> transactions = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    @Override
    public Transaction getTransactionById(int id) {
        return transactions.get(id);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        int id = idCounter.getAndIncrement();
        transaction.setId(id);
        transactions.put(id, transaction);
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        if (!transactions.containsKey(transaction.getId()))
            throw new RuntimeException("Transaction not found");
        transactions.put(transaction.getId(), transaction);
    }

    @Override
    public void deleteTransaction(int id) {
        if(!transactions.containsKey(id))
            throw new RuntimeException("Transaction not found");
        transactions.remove(id);
    }

    @Override
    public List<Transaction> getTransactionsByUserEmail(String email) {
        return transactions.values().stream()
                .filter(t -> t.getBuyersEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }
}
