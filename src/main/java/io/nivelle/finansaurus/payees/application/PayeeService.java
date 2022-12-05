package io.nivelle.finansaurus.payees.application;

import io.nivelle.finansaurus.payees.domain.Payee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayeeService {
    Payee save(Payee payee);

    Payee fetch(long id);

    void delete(long id);

    Page<Payee> list(Pageable pageable);
}
