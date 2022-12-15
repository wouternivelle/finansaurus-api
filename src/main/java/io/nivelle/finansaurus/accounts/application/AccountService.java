package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;

import java.util.List;

public interface AccountService {
    Account save(Account category);

    void delete(long id);

    List<Account> list();

    Account fetch(long id);
}
