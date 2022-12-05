package io.nivelle.finansaurus.transactions.domain;

import io.nivelle.finansaurus.common.NotFoundException;

public class TransactionNotFoundException extends NotFoundException {

    public TransactionNotFoundException(Long id) {
        super(id);
    }
}
