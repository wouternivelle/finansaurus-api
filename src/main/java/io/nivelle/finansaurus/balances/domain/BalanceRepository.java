package io.nivelle.finansaurus.balances.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BalanceRepository extends PagingAndSortingRepository<Balance, Long> {
    @Query("select b from Balance b order by b.year asc, b.month asc")
    List<Balance> findAllOrderByDate();
    Optional<Balance> findByMonthAndYear(int month, int year);
}
