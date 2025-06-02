package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaTransactionRepository;

public class DbTransactionData implements TransactionData {

    private final JpaTransactionRepository repo;

    public DbTransactionData(JpaTransactionRepository repo) {
        this.repo = repo;
    }

    @Override
    public Transaction getTransactionById(int id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return repo.findAll();
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        repo.save(transaction);
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        repo.save(transaction);
    }

    @Override
    public void deleteTransaction(int id) {
        repo.deleteById(id);
    }

    @Override
    public List<Transaction> getTransactionsByUserEmail(String email) {
        return repo.findByBuyersEmail(email);
    }
}
