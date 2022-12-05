package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;

import java.util.List;

public interface BalanceService {
    List<Balance> list(int year, int month);

    Balance save(Balance balance);

    Balance usePreviousMonth(long id);
}
