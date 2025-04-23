package com.SEGroup.Domain;

import java.util.*;

public class TransactionRepository implements ITransactionRepository {
    private final Map<Integer, Transaction> transactions = new HashMap<>(); // <Identifier, Transaction>
    private int nextId = 1; // Identifier for transactions

    @Override
    public void addTransaction(Transaction transaction) {
        transactions.put(nextId++, transaction);
    }

    @Override
    public Transaction getTransactionById(int id) {
        return transactions.get(id);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
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
        transactions.remove(id);
    }

    @Override
    public List<Transaction> getTransactionsByUserEmail(String userEmail) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values()) {
            if (t.getUserEmail().equalsIgnoreCase(userEmail)) {
                result.add(t);
            }
        }
        return result;
    }
}