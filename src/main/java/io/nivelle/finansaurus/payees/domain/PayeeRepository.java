package io.nivelle.finansaurus.payees.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayeeRepository extends PagingAndSortingRepository<Payee, Long> {
    Optional<Payee> findPayeeByName(String name);
}
