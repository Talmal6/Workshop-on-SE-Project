package com.SEGroup.Domain;
import java.util.List;

public interface ITransactionRepository {

    // Define the methods that will be implemented by the concrete class
    void addTransaction(TransactionDTO transaction);
    TransactionDTO getTransactionById(int id);
    List<TransactionDTO> getAllTransactions();
    void updateTransaction(TransactionDTO transaction);
    void deleteTransaction(int id); 
    List<TransactionDTO> getTransactionsByUserEmail(String userEmail);
} 
