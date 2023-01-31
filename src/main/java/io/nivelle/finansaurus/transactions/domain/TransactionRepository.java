package io.nivelle.finansaurus.transactions.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends PagingAndSortingRepository<Transaction, Long> {
    @Query("select t from Transaction t where " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INITIAL') and t.date >= add_months(adddate(last_day(:date), 1), -1) and t.date <= last_day(:date)) OR " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INCOME_CURRENT_MONTH') and t.date >= add_months(adddate(last_day(:date), 1), -1) and t.date <= last_day(:date)) OR " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INCOME_NEXT_MONTH') and t.date >= add_months(adddate(last_day(:date), 1), -2) and t.date <= last_day(add_months(:date, -1)))")
    List<Transaction> listIncomingForBalance(LocalDate date);

    @Query("select t from Transaction t where t.categoryId = :categoryId and t.date >= :start and t.date <= :end")
    List<Transaction> listForPeriodAndCategory(LocalDate start, LocalDate end, Long categoryId);
}
