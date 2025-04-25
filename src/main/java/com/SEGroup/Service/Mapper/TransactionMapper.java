package com.SEGroup.Service.Mapper;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.Product.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TransactionMapper {
    private TransactionMapper() {
        /* ... */ }

    public static TransactionDTO toDTO(Transaction tx) {
        if (tx == null)
            return null;
        return new TransactionDTO(
                tx.getItemsToTransact(),
                tx.getCost(),
                tx.getBuyersEmail(),
                tx.getStoreName());
    }

    public static List<TransactionDTO> toDTO(List<Transaction> txs) {
        if (txs == null)
            return null;
        return txs.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }
}
