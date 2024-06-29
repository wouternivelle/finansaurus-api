package io.nivelle.finansaurus.balances.domain;

import io.nivelle.finansaurus.common.exception.NotFoundException;

public class BalanceNotFoundException extends NotFoundException {

    public BalanceNotFoundException(long id) {
        super(id);
    }

    public BalanceNotFoundException() {
    }
}
