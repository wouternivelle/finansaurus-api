package io.nivelle.finansaurus.payees.application;

import io.nivelle.finansaurus.payees.domain.Payee;

import java.util.List;

public interface PayeeService {
    Payee save(Payee payee);

    Payee fetch(long id);

    void delete(long id);

    List<Payee> list();
}
