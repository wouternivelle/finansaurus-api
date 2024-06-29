package io.nivelle.finansaurus.accounts.domain;

import io.nivelle.finansaurus.common.exception.NotFoundException;

public class AccountNotFoundException extends NotFoundException {

    public AccountNotFoundException(Long id) {
        super(id);
    }
}
