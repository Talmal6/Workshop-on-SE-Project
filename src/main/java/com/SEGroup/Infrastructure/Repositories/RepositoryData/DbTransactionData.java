package com.SEGroup.Infrastructure.Repositories.RepositoryData;

import java.util.List;

import com.SEGroup.Domain.Transaction.Transaction;
import com.SEGroup.Infrastructure.Repositories.JpaDatabase.JpaTransactionRepository;
import static com.SEGroup.Infrastructure.Repositories.RepositoryData.DbSafeExecutor.safeExecute;

public class DbTransactionData implements TransactionData {

    private final JpaTransactionRepository repo;

    public DbTransactionData(JpaTransactionRepository repo) {
        this.repo = repo;
    }

    @Override
    public Transaction getTransactionById(int id) {
        return safeExecute("getTransactionById", () ->
                repo.findById(id).orElse(null));
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return safeExecute("getAllTransactions", repo::findAll);
    }

    @Override
    public void saveTransaction(Transaction transaction) {
        safeExecute("saveTransaction", () -> {
            repo.save(transaction);
            return null;
        });
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        safeExecute("updateTransaction", () -> {
            repo.save(transaction);
            return null;
        });
    }

    @Override
    public void deleteTransaction(int id) {
        safeExecute("deleteTransaction", () -> {
            repo.deleteById(id);
            return null;
        });
    }

    @Override
    public List<Transaction> getTransactionsByUserEmail(String email) {
        return safeExecute("getTransactionsByUserEmail", () ->
                repo.findByBuyersEmail(email));
    }
}
