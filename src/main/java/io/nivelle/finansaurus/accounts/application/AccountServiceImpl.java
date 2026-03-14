package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.application.BalanceService;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);
    private AccountRepository repository;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private BalanceService balanceService;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, TransactionRepository transactionRepository, 
                             CategoryRepository categoryRepository, BalanceService balanceService) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.balanceService = balanceService;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        boolean isNew = account.getId() == null;
        account = repository.save(account);
        
        if (isNew) {
            // Create initial transaction for new account
            Transaction transaction = Transaction.builder()
                    .accountId(account.getId())
                    .type(TransactionType.IN)
                    .amount(account.getAmount())
                    .categoryId(categoryRepository.findCategoryByType(CategoryType.INITIAL).getId())
                    .date(LocalDate.now())
                    .build();
            transactionRepository.save(transaction);
            balanceService.updateForTransactionAdded(transaction);
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

    @Override
    @Transactional
    public void updateBalanceForTransaction(Transaction transaction) {
        Account account = repository.findById(transaction.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(transaction.getAccountId()));

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));
            LOGGER.info("Added {} to account '{}' resulting in total of {}", 
                    transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));
            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", 
                    transaction.getAmount(), account.getName(), account.getAmount());
        }
        repository.save(account);
    }

    @Override
    @Transactional
    public void reverseBalanceForTransaction(Transaction transaction) {
        Account account = repository.findById(transaction.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(transaction.getAccountId()));

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));
            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", 
                    transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));
            LOGGER.info("Added {} to account '{}' resulting in total of {}", 
                    transaction.getAmount(), account.getName(), account.getAmount());
        }
        repository.save(account);
    }
}
