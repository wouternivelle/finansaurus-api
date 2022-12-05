package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    Account save(Account category);

    void delete(long id);

    Page<Account> list(Pageable pageable);

    Account fetch(long id);
}
