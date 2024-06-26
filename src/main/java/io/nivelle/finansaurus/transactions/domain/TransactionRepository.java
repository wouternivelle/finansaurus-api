package io.nivelle.finansaurus.transactions.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long>, PagingAndSortingRepository<Transaction, Long> {
    @Query("select t from Transaction t where " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INITIAL') and t.date >= :startCurrent and t.date <= :endCurrent) OR " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INCOME_CURRENT_MONTH') and t.date >= :startCurrent and t.date <= :endCurrent) OR " +
            "(t.categoryId = (select c.id from Category c where c.type = 'INCOME_NEXT_MONTH') and t.date >= :startPrevious and t.date <= :endPrevious)")
    List<Transaction> listIncomingForBalance(LocalDate startCurrent, LocalDate endCurrent, LocalDate startPrevious, LocalDate endPrevious);

    @Query("select t from Transaction t where t.categoryId = :categoryId and t.date >= :start and t.date <= :end")
    List<Transaction> listForPeriodAndCategory(LocalDate start, LocalDate end, Long categoryId);

    @Query("select new io.nivelle.finansaurus.transactions.domain.PeriodicalReport(sum(t.amount), c.name) from Transaction t inner join Category c on c.id = t.categoryId where t.date >= :start and t.date <= :end and t.type = 'OUT' group by t.categoryId")
    List<PeriodicalReport> reportOutgoingForPeriod(LocalDate start, LocalDate end);
}
