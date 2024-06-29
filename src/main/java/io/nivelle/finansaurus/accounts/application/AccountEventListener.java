package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import io.nivelle.finansaurus.transactions.domain.event.TransactionAddedEvent;
import io.nivelle.finansaurus.transactions.domain.event.TransactionDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AccountEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEventListener.class);
    private final AccountRepository accountRepository;

    @Autowired
    public AccountEventListener(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @EventListener
    public void onTransactionAdded(TransactionAddedEvent event) {
        Transaction transaction = event.transaction();
        Account account = getAccount(transaction);

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));

            LOGGER.info("Added {} to account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));

            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        }
        accountRepository.save(account);
    }

    @EventListener
    public void onTransactionDeleted(TransactionDeletedEvent event) {
        Transaction transaction = event.transaction();

        Account account = getAccount(transaction);

        if (transaction.getType().equals(TransactionType.IN)) {
            account.updateAmount(account.getAmount().subtract(transaction.getAmount()));

            LOGGER.info("Subtracted {} from account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        } else {
            account.updateAmount(account.getAmount().add(transaction.getAmount()));

            LOGGER.info("Added {} to account '{}' resulting in total of {}", transaction.getAmount(), account.getName(), account.getAmount());
        }
        accountRepository.save(account);
    }

    private Account getAccount(Transaction transaction) {
        Account account = accountRepository.findById(transaction.getAccountId()).orElseThrow(() -> new AccountNotFoundException(transaction.getAccountId()));
        return account;
    }

}
