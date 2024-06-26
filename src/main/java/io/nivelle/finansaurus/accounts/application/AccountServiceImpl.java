package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.accounts.domain.event.AccountSavedEvent;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.common.domain.DomainEventPublisher;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository repository;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private BalanceRepository balanceRepository;
    private DomainEventPublisher publisher;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, TransactionRepository transactionRepository, CategoryRepository categoryRepository, BalanceRepository balanceRepository, DomainEventPublisher publisher) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.balanceRepository = balanceRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        boolean isNew = account.getId() == null;

        account = repository.save(account);

        publisher.publish(new AccountSavedEvent(account, isNew));

        if (isNew) {
            Transaction transaction = Transaction.builder().accountId(account.getId()).type(TransactionType.IN).amount(account.getAmount()).categoryId(categoryRepository.findCategoryByType(CategoryType.INITIAL).getId()).date(LocalDate.now()).build();
            transactionRepository.save(transaction);

            addToBalance(transaction);
        }

        return account;
    }

    @Override
    public void delete(long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException exc) {
            throw new AccountNotFoundException(id);
        }
    }

    @Override
    public Account fetch(long id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public List<Account> list() {
        return repository.findAll();
    }

    private void addToBalance(Transaction transaction) {
        Balance balance = findOrCreateBalance(transaction);

        balance.updateIncoming(balance.getIncoming().add(transaction.getAmount()));
        balanceRepository.save(balance);
    }

    private Balance findOrCreateBalance(Transaction transaction) {
        LocalDate date = transaction.getDate();

        int month = date.getMonthValue();
        int year = date.getYear();

        return balanceRepository.findByMonthAndYear(month, year)
                .orElseGet(() -> balanceRepository.save(Balance.builder().month(month).year(year).build()));
    }
}
