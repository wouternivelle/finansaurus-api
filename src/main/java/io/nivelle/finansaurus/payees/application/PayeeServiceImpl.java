package io.nivelle.finansaurus.payees.application;

import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.payees.domain.PayeeNotFoundException;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PayeeServiceImpl implements PayeeService {
    private PayeeRepository repository;

    @Autowired
    PayeeServiceImpl(PayeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payee save(Payee payee) {
        return repository.save(payee);
    }

    @Override
    public Payee fetch(long id) {
        return repository.findById(id).orElseThrow(() -> new PayeeNotFoundException(id));
    }

    @Override
    public void delete(long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException exc) {
            throw new PayeeNotFoundException(id);
        }
    }

    @Override
    public List<Payee> list() {
        return repository.findAll();
    }
}
