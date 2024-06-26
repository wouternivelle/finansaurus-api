package io.nivelle.finansaurus.payees.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayeeRepository extends CrudRepository<Payee, Long>, PagingAndSortingRepository<Payee, Long> {
    List<Payee> findAll();

    Optional<Payee> findPayeeByName(String name);
}
