package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import io.nivelle.finansaurus.transactions.domain.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
public class TransactionServiceImpl implements TransactionService {
    private TransactionRepository repository;
    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private BalanceRepository balanceRepository;
    private PayeeRepository payeeRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    TransactionServiceImpl(TransactionRepository repository, AccountRepository accountRepository, CategoryRepository categoryRepository, BalanceRepository balanceRepository, PayeeRepository payeeRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.balanceRepository = balanceRepository;
        this.payeeRepository = payeeRepository;
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
        addToAccount(transaction);
        addToBalance(transaction);
        findOrCreatePayee(transaction);

        repository.save(transaction);

        LOGGER.info("Added {}", transaction);
    }

    private void findOrCreatePayee(Transaction transaction) {
        Optional<Payee> foundPayee = payeeRepository.findPayeeByName(transaction.getPayeeName());
        foundPayee.ifPresentOrElse(payee -> transaction.updatePayee(payee.getId()), () -> {
            Payee payee = payeeRepository.save(Payee.builder()
                    .name(transaction.getPayeeName())
                    .lastCategoryId(transaction.getCategoryId())
                    .build());
            transaction.updatePayee(payee.getId());
        });
    }

    private void addToBalance(Transaction transaction) {
        Category incomeNextMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_NEXT_MONTH);
        boolean isIncomeNextMonth = transaction.getCategoryId().equals(incomeNextMonth.getId());
        Balance balance = findOrCreateBalance(transaction, isIncomeNextMonth);

        if (isIncoming(transaction)) {
            balance.updateIncoming(balance.getIncoming().add(transaction.getAmount()));
        } else {
            BigDecimal amount;
            if (TransactionType.IN.equals(transaction.getType())) {
                amount = transaction.getAmount().negate();
            } else {
                amount = transaction.getAmount();
            }
            updateCategoryOutflow(balance, amount, transaction.getCategoryId());
        }
        balanceRepository.save(balance);
    }

    private boolean isIncoming(Transaction transaction) {
        return categoryRepository.isIncomingCategory(transaction.getCategoryId());
    }

    private void addToAccount(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccountId()).orElseThrow(() -> new AccountNotFoundException(transaction.getAccountId()));

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));

            LOGGER.info("Added {} to account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));

            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        }
        accountRepository.save(account);
    }

    private void deleteTransaction(Transaction transaction) {
        deleteFromAccount(transaction);
        deleteFromBalance(transaction);

        repository.deleteById(transaction.getId());

        LOGGER.info("Deleted {}", transaction);
    }

    private void deleteFromAccount(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccountId()).orElseThrow(() -> new AccountNotFoundException(transaction.getAccountId()));

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));

            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));

            LOGGER.info("Added {} to account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        }
        accountRepository.save(account);
    }

    private void deleteFromBalance(Transaction transaction) {
        Category incomeNextMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_NEXT_MONTH);
        boolean isIncomeNextMonth = transaction.getCategoryId().equals(incomeNextMonth.getId());
        Balance balance = findOrCreateBalance(transaction, isIncomeNextMonth);
        if (isIncoming(transaction)) {
            balance.updateIncoming(balance.getIncoming().subtract(transaction.getAmount()));
        } else {
            BigDecimal amount;
            if (TransactionType.IN.equals(transaction.getType())) {
                amount = transaction.getAmount();
            } else {
                amount = transaction.getAmount().negate();
            }
            updateCategoryOutflow(balance, amount, transaction.getCategoryId());
        }
        balanceRepository.save(balance);
    }

    private Balance findOrCreateBalance(Transaction transaction, boolean isIncomeNextMonth) {
        LocalDate date = transaction.getDate();
        if (isIncomeNextMonth) {
            date = date.plusMonths(1);
        }

        int month = date.getMonthValue();
        int year = date.getYear();

        return balanceRepository.findByMonthAndYear(month, year)
                .orElseGet(() -> balanceRepository.save(Balance.builder().month(month).year(year).build()));
    }

    private void updateCategoryOutflow(Balance balance, BigDecimal amount, Long categoryId) {
        Optional<BalanceCategory> balanceCategory = balance.getCategories().stream()
                .filter(category -> category.getCategoryId().equals(categoryId))
                .findAny();
        balanceCategory.ifPresentOrElse(category -> category.updateOperations(category.getOperations().add(amount)),
                () -> balance.getCategories().add(BalanceCategory.builder().categoryId(categoryId).operations(amount).build()));
    }

    @Override
    @Transactional
    public void delete(long id) {
        Transaction transaction = fetch(id);
        deleteTransaction(transaction);
    }

    @Override
    public Page<Transaction> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Transaction fetch(long id) {
        return repository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Override
    public List<Transaction> listIncomingForBalance(int year, int month) {
        YearMonth currentMonth = YearMonth.of(year, month);
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate startCurrent = currentMonth.atDay(1);
        LocalDate endCurrent = currentMonth.atEndOfMonth();
        LocalDate startPrevious = previousMonth.atDay(1);
        LocalDate endPrevious = previousMonth.atEndOfMonth();

        return repository.listIncomingForBalance(startCurrent, endCurrent, startPrevious, endPrevious);
    }

    @Override
    public List<Transaction> listForMonthAndCategory(int month, int year, Long categoryId) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(lastDayOfMonth());

        return repository.listForPeriodAndCategory(start, end, categoryId);
    }

    @Override
    public List<PeriodicalReport> reportOutgoingForPeriod(LocalDate start, LocalDate end) {
        return repository.reportOutgoingForPeriod(start, end);
    }
}
