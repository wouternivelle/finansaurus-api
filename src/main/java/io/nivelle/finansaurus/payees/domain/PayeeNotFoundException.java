package io.nivelle.finansaurus.payees.domain;

import io.nivelle.finansaurus.common.exception.NotFoundException;

public class PayeeNotFoundException extends NotFoundException {

    public PayeeNotFoundException(Long id) {
        super(id);
    }
}
