package io.nivelle.finansaurus.accounts.domain;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {
    List<Account> findAll();
}
