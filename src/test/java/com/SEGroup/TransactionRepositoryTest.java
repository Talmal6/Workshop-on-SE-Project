//package com.SEGroup;
//
//import com.SEGroup.Domain.*;
//import com.SEGroup.Domain.Transaction.Transaction;
//import com.SEGroup.Domain.Transaction.TransactionRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TransactionRepositoryTest {
//    private TransactionRepository repo;
//
//    @BeforeEach
//    public void setUp() {
//        repo = new TransactionRepository();
//    }
//
//    @Test
//    public void testAddTransaction() {
//        Transaction t = new Transaction("Yuval@post.bgu.ac.il", 99.99);
//        repo.addTransaction(t);
//        List<Transaction> list = repo.getAllTransactions();
//        assertEquals(1, list.size());
//        assertEquals("Yuval@post.bgu.ac.il", list.get(0).getUserEmail());
//    }
//
//    @Test
//    public void testGetByUserEmail() {
//        repo.addTransaction(new Transaction("Yuval@post.bgu.ac.il", 10));
//        repo.addTransaction(new Transaction("Yuval@post.bgu.ac.il", 15));
//        repo.addTransaction(new Transaction("otherUser@post.bgu.ac.il", 20));
//        List<Transaction> result = repo.getTransactionsByUserEmail("Yuval@post.bgu.ac.il");
//        assertEquals(2, result.size());
//    }
//
//    @Test
//    public void testDeleteTransaction() {
//        Transaction t1 = new Transaction("Yuval@post.bgu.ac.il", 33);
//        Transaction t2 = new Transaction("otherUser@post.bgu.ac.il", 44);
//        repo.addTransaction(t1);
//        repo.addTransaction(t2);
//        repo.deleteTransaction(1);
//        assertEquals(1, repo.getAllTransactions().size());
//    }
//}
