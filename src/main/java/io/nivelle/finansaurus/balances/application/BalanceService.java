package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.transactions.domain.Transaction;

import java.util.List;

public interface BalanceService {
    List<Balance> list(int year, int month);

    Balance save(Balance balance);

    Balance usePreviousMonth(long id);
    
    void updateForTransactionAdded(Transaction transaction);
    
    void updateForTransactionDeleted(Transaction transaction);
}
