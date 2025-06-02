package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import com.SEGroup.Domain.Transaction.Transaction;

import java.util.List;

public interface TransactionData {
    Transaction getTransactionById(int id);
    List<Transaction> getAllTransactions();
    void saveTransaction(Transaction transaction);
    void updateTransaction(Transaction transaction);
    void deleteTransaction(int id);
    List<Transaction> getTransactionsByUserEmail(String email);
}
