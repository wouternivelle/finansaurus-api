package io.nivelle.finansaurus.transactions.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private BalanceRepository balanceRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PayeeRepository payeeRepository;

    @BeforeEach
    public void setup() {
        service = new TransactionServiceImpl(repository, accountRepository, categoryRepository, balanceRepository, payeeRepository);
    }

    @Test
    public void whenSavingNewTransaction_withInvalidAccount_thenExceptionThrown() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        when(accountRepository.findById(eq(1L)))
                .thenThrow(new AccountNotFoundException(1L));

        assertThrows(AccountNotFoundException.class, () -> service.save(transaction));
    }

    @Test
    public void whenSavingNewOutTransactionWithNewPayee_thenAccountSubtractedAndExistingBalanceCategoryUpdated() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));
        when(payeeRepository.save(any(Payee.class)))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("-500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().get(0).getOperations(), equalTo(new BigDecimal("500.0")));
        assertThat(balance.getCategories().get(0).getCategoryId(), equalTo(1L));
        ArgumentCaptor<Payee> payeeCaptor = ArgumentCaptor.forClass(Payee.class);
        verify(payeeRepository).save(payeeCaptor.capture());
        Payee payee = payeeCaptor.getValue();
        assertThat(payee.getName(), equalTo(transaction.getPayeeName()));
        assertThat(payee.getLastCategoryId(), equalTo(transaction.getCategoryId()));
        verify(repository).save(eq(transaction));
    }

    @Test
    public void whenSavingNewInTransaction_thenAccountAddedAndExistingBalanceCategoryUpdated() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));
        when(payeeRepository.save(any(Payee.class)))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().get(0).getOperations(), equalTo(new BigDecimal("-500.0")));
        assertThat(balance.getCategories().get(0).getCategoryId(), equalTo(1L));
        verify(repository).save(eq(transaction));
    }

    @Test
    public void whenSavingNewIncomingTransactionWithExistingPayee_thenAccountAddedAndNewBalanceIncomingUpdated() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().id(1L).build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(true);
        when(balanceRepository.findByMonthAndYear(transaction.getDate().plusMonths(1).getMonthValue(), transaction.getDate().plusMonths(1).getYear()))
                .thenReturn(Optional.empty());
        Balance balance = Balance.builder().month(transaction.getDate().plusMonths(1).getMonthValue()).year(transaction.getDate().plusMonths(1).getYear()).build();
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(balance);
        when(payeeRepository.findPayeeByName("payee 1"))
                .thenReturn(Optional.of(Payee.builder().id(1L).build()));

        service.save(transaction);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getMonth(), equalTo(transaction.getDate().plusMonths(1).getMonthValue()));
        assertThat(balance.getYear(), equalTo(transaction.getDate().plusMonths(1).getYear()));
        assertThat(balance.getIncoming(), equalTo(new BigDecimal("500.0")));
        verify(repository).save(eq(transaction));
        assertThat(transaction.getPayeeId(), equalTo(1L));
        verify(payeeRepository, times(0)).save(any(Payee.class));
    }

    @Test
    public void whenSavingExistingOutTransaction_thenBothAddAndDeleteExecuted() {
        Transaction transaction = buildExistingTransaction(TransactionType.OUT);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));
        when(payeeRepository.save(any(Payee.class)))
                .thenReturn(Payee.builder().id(1L).build());

        service.save(transaction);

        verify(accountRepository, times(2)).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("0.0")));
        verify(balanceRepository, times(2)).save(eq(balance));
        assertThat(balance.getCategories().get(0).getOperations(), equalTo(new BigDecimal("0.0")));
        assertThat(balance.getCategories().get(0).getCategoryId(), equalTo(1L));
        ArgumentCaptor<Payee> payeeCaptor = ArgumentCaptor.forClass(Payee.class);
        verify(payeeRepository).save(payeeCaptor.capture());
        Payee payee = payeeCaptor.getValue();
        assertThat(payee.getName(), equalTo(transaction.getPayeeName()));
        assertThat(payee.getLastCategoryId(), equalTo(transaction.getCategoryId()));
        verify(repository).deleteById(eq(1L));
        verify(repository).save(any(Transaction.class));
    }

    @Test
    public void whenDeletingOutTransaction_thenAccountAddedAndExistingBalanceCategoryUpdated() {
        Transaction transaction = buildExistingTransaction(TransactionType.OUT);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        service.delete(1L);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().get(0).getOperations(), equalTo(new BigDecimal("-500.0")));
        assertThat(balance.getCategories().get(0).getCategoryId(), equalTo(1L));
        verify(repository).deleteById(eq(1L));
    }

    @Test
    public void whenDeletingInTransaction_thenAccountSubtractedAndNewBalanceCategoryUpdated() {
        Transaction transaction = buildExistingTransaction(TransactionType.IN);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        service.delete(1L);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("-500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().get(0).getOperations(), equalTo(new BigDecimal("500.0")));
        assertThat(balance.getCategories().get(0).getCategoryId(), equalTo(1L));
        verify(repository).deleteById(eq(1L));
    }

    @Test
    public void whenDeletingIncomingTransaction_thenAccountSubtractedAndNewBalanceIncomingUpdated() {
        Transaction transaction = buildExistingTransaction(TransactionType.IN);
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(transaction));
        Account account = Account.builder().build();
        when(accountRepository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Category category = Category.builder().id(1L).build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(true);
        when(balanceRepository.findByMonthAndYear(transaction.getDate().plusMonths(1).getMonthValue(), transaction.getDate().plusMonths(1).getYear()))
                .thenReturn(Optional.empty());
        Balance balance = Balance.builder().month(transaction.getDate().plusMonths(1).getMonthValue()).year(transaction.getDate().plusMonths(1).getYear()).build();
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(balance);

        service.delete(1L);

        verify(accountRepository).save(eq(account));
        assertThat(account.getAmount(), equalTo(new BigDecimal("-500.0")));
        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getMonth(), equalTo(transaction.getDate().plusMonths(1).getMonthValue()));
        assertThat(balance.getYear(), equalTo(transaction.getDate().plusMonths(1).getYear()));
        assertThat(balance.getIncoming(), equalTo(new BigDecimal("-500.0")));
        verify(repository).deleteById(eq(1L));
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

    private Transaction buildTransaction(TransactionType type) {
        return Transaction.builder()
                .date(LocalDate.now())
                .payeeName("payee 1")
                .type(type)
                .categoryId(1L)
                .accountId(1L)
                .amount(new BigDecimal("500.0"))
                .note("a note")
                .build();
    }

    private Transaction buildExistingTransaction(TransactionType type) {
        return Transaction.builder()
                .id(1L)
                .date(LocalDate.now())
                .payeeName("payee 1")
                .type(type)
                .categoryId(1L)
                .accountId(1L)
                .amount(new BigDecimal("500.0"))
                .note("a note")
                .build();
    }
}
