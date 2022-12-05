package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;

@Service
public class AccountServiceImpl implements AccountService {
    private AccountRepository repository;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository repository, TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public Account save(Account account) {
        boolean isNew = account.getId() == null;

        account = repository.save(account);

        if (isNew) {
            Transaction transaction = Transaction.builder().accountId(account.getId()).type(TransactionType.IN).amount(account.getAmount()).categoryId(categoryRepository.findCategoryByType(CategoryType.INITIAL).getId()).date(LocalDate.now()).build();
            transactionRepository.save(transaction);
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
    public Page<Account> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
