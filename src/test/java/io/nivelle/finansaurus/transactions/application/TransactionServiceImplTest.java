package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.accounts.application.AccountService;
import io.nivelle.finansaurus.balances.application.BalanceService;
import io.nivelle.finansaurus.payees.application.PayeeService;
import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.transactions.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static io.nivelle.finansaurus.transactions.application.TransactionTestData.buildExistingTransaction;
import static io.nivelle.finansaurus.transactions.application.TransactionTestData.buildTransaction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {
    private TransactionService service;

    @Mock
    private TransactionRepository repository;
    @Mock
    private PayeeService payeeService;
    @Mock
    private AccountService accountService;
    @Mock
    private BalanceService balanceService;

    @BeforeEach
    public void setup() {
        service = new TransactionServiceImpl(repository, payeeService, accountService, balanceService);
    }

    @Test
    public void whenSavingNewOutTransactionWithNewPayee_thenTransactionSavedAndPayeeCreated() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        when(payeeService.findOrCreateByName(eq(transaction.getPayeeName()), eq(transaction.getCategoryId())))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(payeeService).findOrCreateByName(eq(transaction.getPayeeName()), eq(transaction.getCategoryId()));
        verify(repository).save(eq(transaction));
        verify(accountService).updateBalanceForTransaction(eq(transaction));
        verify(balanceService).updateForTransactionAdded(eq(transaction));
    }

    @Test
    public void whenSavingExistingOutTransaction_thenBothAddAndDeleteExecuted() {
        Transaction transaction = buildExistingTransaction(TransactionType.OUT);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));
        when(payeeService.findOrCreateByName(eq(transaction.getPayeeName()), eq(transaction.getCategoryId())))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(payeeService).findOrCreateByName(eq(transaction.getPayeeName()), eq(transaction.getCategoryId()));
        verify(repository).deleteById(eq(1L));
        verify(repository).save(any(Transaction.class));
        verify(accountService).reverseBalanceForTransaction(eq(transaction));
        verify(balanceService).updateForTransactionDeleted(eq(transaction));
        verify(accountService).updateBalanceForTransaction(any(Transaction.class));
        verify(balanceService).updateForTransactionAdded(any(Transaction.class));
    }

    @Test
    public void whenSavingNewIncomingTransactionWithExistingPayee_thenTransactionAndPayeeSaved() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        when(payeeService.findOrCreateByName(eq("payee 1"), eq(transaction.getCategoryId())))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(repository).save(eq(transaction));
        assertThat(transaction.getPayeeId(), equalTo(1L));
        verify(accountService).updateBalanceForTransaction(eq(transaction));
        verify(balanceService).updateForTransactionAdded(eq(transaction));
    }

    @Test
    public void whenDeletingOutTransaction_thenAccountAndBalanceUpdated() {
        Transaction transaction = buildExistingTransaction(TransactionType.OUT);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));

        service.delete(1L);

        verify(repository).deleteById(eq(1L));
        verify(accountService).reverseBalanceForTransaction(eq(transaction));
        verify(balanceService).updateForTransactionDeleted(eq(transaction));
    }

    @Test
    public void whenListing_thenListReturned() {
        Pageable pageable = mock(Pageable.class);
        when(repository.findAll(eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(Transaction.builder().build())));

        Page<Transaction> result = service.list(pageable);

        assertThat(result.getContent(), hasSize(1));
        verify(repository).findAll(eq(pageable));
    }

    @Test
    public void whenFetchingExistingTransaction_thenTransactionReturned() {
        Transaction transaction = Transaction.builder().build();
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));

        Transaction result = service.fetch(1);

        assertThat(result, equalTo(transaction));
        verify(repository).findById(eq(1L));
    }

    @Test
    public void whenFetchingNonExistingTransaction_thenExceptionThrown() {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> service.fetch(1));
    }

    @Test
    public void whenListIncomingForBalance_thenListReturned() {
        when(repository.listIncomingForBalance(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31)), eq(LocalDate.of(2022, 12, 1)), eq(LocalDate.of(2022, 12, 31))))
                .thenReturn(List.of(Transaction.builder().build()));

        List<Transaction> result = service.listIncomingForBalance(2023, 1);

        assertThat(result, hasSize(1));
        verify(repository).listIncomingForBalance(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31)), eq(LocalDate.of(2022, 12, 1)), eq(LocalDate.of(2022, 12, 31)));
    }

    @Test
    public void whenListForMonthAndCategory_thenListReturned() {
        when(repository.listForPeriodAndCategory(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31)), eq(1L)))
                .thenReturn(List.of(Transaction.builder().build()));

        List<Transaction> result = service.listForMonthAndCategory(1, 2023, 1L);

        assertThat(result, hasSize(1));
        verify(repository).listForPeriodAndCategory(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31)), eq(1L));
    }

    @Test
    public void whenReporting_thenCorrectListReturned() {
        when(repository.reportOutgoingForPeriod(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31))))
                .thenReturn(List.of(PeriodicalReport.builder().build()));

        List<PeriodicalReport> result = service.reportOutgoingForPeriod(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31));

        assertThat(result, hasSize(1));
        verify(repository).reportOutgoingForPeriod(eq(LocalDate.of(2023, 1, 1)), eq(LocalDate.of(2023, 1, 31)));
    }
}
