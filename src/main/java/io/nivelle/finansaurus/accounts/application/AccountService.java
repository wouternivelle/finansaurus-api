package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.transactions.domain.Transaction;

import java.util.List;

public interface AccountService {
    Account save(Account account);

    void delete(long id);

    List<Account> list();

    Account fetch(long id);
    
    void updateBalanceForTransaction(Transaction transaction);
    
    void reverseBalanceForTransaction(Transaction transaction);
}
