//package com.SEGroup;
//
//import org.junit.jupiter.api.Test;
//
//import com.SEGroup.Domain.Transaction.Transaction;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class TransactionTest {
//    @Test
//    public void testTransactionFields() {
//        Transaction t = new Transaction("Yuval@post.bgu.ac.il", 150.0);
//        assertEquals("Yuval@post.bgu.ac.il", t.getUserEmail());
//        assertEquals(150.0, t.getAmount());
//    }
//
//    @Test
//    public void testSetters() {
//        Transaction t = new Transaction("Yuval@post.bgu.ac.il", 100);
//        t.setUserEmail("Yuval1@post.bgu.ac.il");
//        t.setAmount(200);
//        assertEquals("Yuval1@post.bgu.ac.il", t.getUserEmail());
//        assertEquals(200, t.getAmount());
//    }
//}