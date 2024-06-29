package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import io.nivelle.finansaurus.transactions.domain.event.TransactionAddedEvent;
import io.nivelle.finansaurus.transactions.domain.event.TransactionDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static io.nivelle.finansaurus.transactions.application.TransactionTestData.buildTransaction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountEventListenerTest {
    private AccountEventListener listener;
    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        listener = new AccountEventListener(accountRepository);
    }

    @Test
    public void whenTransactionAddedWithInvalidAccount_thenExceptionThrown() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        when(accountRepository.findById(eq(1L)))
                .thenThrow(new AccountNotFoundException(1L));

        assertThrows(AccountNotFoundException.class, () -> listener.onTransactionAdded(new TransactionAddedEvent(transaction)));
    }

    @Test
    public void whenIncomingTransactionAdded_thenAccountIncreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L))).thenReturn(Optional.of(account));

        listener.onTransactionAdded(new TransactionAddedEvent(transaction));

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("500.0")));
    }

    @Test
    public void whenOutgoingTransactionAdded_thenAccountDecreased() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L))).thenReturn(Optional.of(account));

        listener.onTransactionAdded(new TransactionAddedEvent(transaction));

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("-500.0")));
    }

    @Test
    public void whenIncomingTransactionDeleted_thenAccountDecreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L))).thenReturn(Optional.of(account));

        listener.onTransactionDeleted(new TransactionDeletedEvent(transaction));

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("-500.0")));
    }

    @Test
    public void whenOutgoingTransactionDeleted_thenAccountIncreased() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L))).thenReturn(Optional.of(account));

        listener.onTransactionDeleted(new TransactionDeletedEvent(transaction));

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("500.0")));
    }
}
