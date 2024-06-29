package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionTestData {
    public static Transaction buildTransaction(TransactionType type) {
        return Transaction.builder()
                .date(LocalDate.now())
                .payeeName("payee 1")
                .type(type)
                .categoryId(1L)
                .accountId(1L)
                .amount(new BigDecimal("500.0"))
                .note("a note")
                .build();
    }

    public static Transaction buildExistingTransaction(TransactionType type) {
        return Transaction.builder()
                .id(1L)
                .date(LocalDate.now())
                .payeeName("payee 1")
                .type(type)
                .categoryId(1L)
                .accountId(1L)
                .amount(new BigDecimal("500.0"))
                .note("a note")
                .build();
    }
}
