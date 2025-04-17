package com.SEGroup.Domain;
import java.util.List;

public interface ITransactionRepository {

    // Define the methods that will be implemented by the concrete class
    void addTransaction(Transaction transaction);
    Transaction getTransactionById(int id);
    List<Transaction> getAllTransactions();
    void updateTransaction(Transaction transaction);
    void deleteTransaction(int id); 
} 
