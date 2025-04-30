package com.SEGroup.Mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.SEGroup.DTO.TransactionDTO;
import com.SEGroup.Domain.Transaction.Transaction;

/**
 * Utility class for converting Transaction domain objects to TransactionDTO data transfer objects.
 * Provides methods to map a single Transaction object or a list of Transaction objects to TransactionDTOs.
 */
public final class TransactionMapper {

    // Private constructor to prevent instantiation of this utility class
    private TransactionMapper() {
        /* ... */
    }

    /**
     * Converts a single Transaction domain object to a TransactionDTO.
     *
     * @param tx The Transaction domain object to convert.
     * @return A TransactionDTO representing the Transaction domain object, or null if the input is null.
     */
    public static TransactionDTO toDTO(Transaction tx) {
        if (tx == null)
            return null;

        return new TransactionDTO(
                tx.getItemsToTransact(),
                tx.getCost(),
                tx.getBuyersEmail(),
                tx.getStoreName());
    }

    /**
     * Converts a list of Transaction domain objects to a list of TransactionDTOs.
     *
     * @param txs The list of Transaction domain objects to convert.
     * @return A list of TransactionDTOs representing the given list of Transaction domain objects,
     *         or null if the input list is null.
     */
    public static List<TransactionDTO> toDTO(List<Transaction> txs) {
        if (txs == null)
            return null;

        return txs.stream()
                .map(TransactionMapper::toDTO)  // Convert each Transaction to a TransactionDTO
                .collect(Collectors.toList());  // Collect the converted DTOs into a list
    }
}
