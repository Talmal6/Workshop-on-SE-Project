package com.SEGroup.Domain;
import java.util.List;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.Transaction.Transaction;

public interface ITransactionRepository {

    // Define the methods that will be implemented by the concrete class
    void addTransaction(List<String> shoppingProductIds, double cost, String buyersEmail, String storeName);
    TransactionDTO getTransactionById(int id);
    List<TransactionDTO> getAllTransactions();
    void updateTransaction(Transaction transaction);
    void deleteTransaction(int id); 
    
    List<TransactionDTO> getTransactionsByUserEmail(String userEmail);
} 
