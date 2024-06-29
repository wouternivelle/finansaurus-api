package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.common.domain.DomainEventPublisher;
import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import io.nivelle.finansaurus.transactions.domain.PeriodicalReport;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionNotFoundException;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import io.nivelle.finansaurus.transactions.domain.event.TransactionAddedEvent;
import io.nivelle.finansaurus.transactions.domain.event.TransactionDeletedEvent;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final PayeeRepository payeeRepository;
    private final DomainEventPublisher publisher;

    @Autowired
    TransactionServiceImpl(TransactionRepository transactionRepository, PayeeRepository payeeRepository, DomainEventPublisher publisher) {
        this.transactionRepository = transactionRepository;
        this.payeeRepository = payeeRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            addTransaction(transaction);
        } else {
            Transaction oldTransaction = fetch(transaction.getId());
            deleteTransaction(oldTransaction);
            addTransaction(Transaction.builder()
                    .date(transaction.getDate())
                    .payeeName(transaction.getPayeeName())
                    .type(transaction.getType())
                    .categoryId(transaction.getCategoryId())
                    .accountId(transaction.getAccountId())
                    .amount(transaction.getAmount())
                    .note(transaction.getNote())
                    .build());
        }
        return transaction;
    }

    private void addTransaction(Transaction transaction) {
        Optional<Payee> foundPayee = payeeRepository.findPayeeByName(transaction.getPayeeName());
        foundPayee.ifPresentOrElse(payee -> transaction.updatePayee(payee.getId()), () -> {
            Payee payee = payeeRepository.save(Payee.builder()
                    .name(transaction.getPayeeName())
                    .lastCategoryId(transaction.getCategoryId())
                    .build());
            transaction.updatePayee(payee.getId());
        });

        transactionRepository.save(transaction);

        publisher.publish(new TransactionAddedEvent(transaction));

        LOGGER.info("Added {}", transaction);
    }

    private void deleteTransaction(Transaction transaction) {
        transactionRepository.deleteById(transaction.getId());

        publisher.publish(new TransactionDeletedEvent(transaction));

        LOGGER.info("Deleted {}", transaction);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Transaction transaction = fetch(id);
        deleteTransaction(transaction);
    }

    @Override
    public Page<Transaction> list(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Transaction fetch(long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Override
    public List<Transaction> listIncomingForBalance(int year, int month) {
        YearMonth currentMonth = YearMonth.of(year, month);
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate startCurrent = currentMonth.atDay(1);
        LocalDate endCurrent = currentMonth.atEndOfMonth();
        LocalDate startPrevious = previousMonth.atDay(1);
        LocalDate endPrevious = previousMonth.atEndOfMonth();

        return transactionRepository.listIncomingForBalance(startCurrent, endCurrent, startPrevious, endPrevious);
    }

    @Override
    public List<Transaction> listForMonthAndCategory(int month, int year, Long categoryId) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(lastDayOfMonth());

        return transactionRepository.listForPeriodAndCategory(start, end, categoryId);
    }

    @Override
    public List<PeriodicalReport> reportOutgoingForPeriod(LocalDate start, LocalDate end) {
        return transactionRepository.reportOutgoingForPeriod(start, end);
    }
}
