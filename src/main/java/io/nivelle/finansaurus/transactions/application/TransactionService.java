package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.transactions.domain.PeriodicalReport;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Transaction save(Transaction transaction);

    void delete(long id);

    Page<Transaction> list(Pageable pageable);

    Transaction fetch(long id);

    List<Transaction> listIncomingForBalance(int year, int month);

    List<Transaction> listForMonthAndCategory(int month, int year, Long categoryId);

    List<PeriodicalReport> reportOutgoingForPeriod(LocalDate start, LocalDate end);
}
