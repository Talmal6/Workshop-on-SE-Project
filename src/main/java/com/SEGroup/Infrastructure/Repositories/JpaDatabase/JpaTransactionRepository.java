package com.SEGroup.Infrastructure.Repositories.JpaDatabase;

import com.SEGroup.Domain.Transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaTransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query(value = """
        SELECT * FROM transactions
        WHERE buyers_email = :email
    """, nativeQuery = true)
    List<Transaction> getTransactionsByBuyerEmail(String email);

    List<Transaction> findByBuyersEmail(String email);

}