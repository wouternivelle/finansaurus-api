package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    private AccountService service;

    @Mock
    private AccountRepository repository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @BeforeEach
    public void setup() {
        service = new AccountServiceImpl(repository, transactionRepository, categoryRepository, balanceRepository);
    }

    @Test
    public void whenSavingNewAccount_thenAccountAndInitialTransactionAndNewBalanceCreated() {
        Account account = Account.builder().amount(new BigDecimal("100")).build();
        when(repository.save(eq(account)))
                .thenReturn(Account.builder().id(1L).amount(new BigDecimal("100")).build());
        when(categoryRepository.findCategoryByType(eq(CategoryType.INITIAL)))
                .thenReturn(Category.builder().id(1L).build());
        Transaction transaction = Transaction.builder().accountId(1L).type(TransactionType.IN).amount(account.getAmount()).categoryId(1L).date(LocalDate.now()).build();
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);
        when(balanceRepository.findByMonthAndYear(eq(LocalDate.now().getMonthValue()), eq(LocalDate.now().getYear())))
                .thenReturn(Optional.empty());
        Balance balance = Balance.builder().month(LocalDate.now().getMonthValue()).year(LocalDate.now().getYear()).build();
        when(balanceRepository.save(any(Balance.class)))
                .thenReturn(balance);

        service.save(account);

        verify(categoryRepository).findCategoryByType(eq(CategoryType.INITIAL));
        verify(repository).save(eq(account));
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        transaction = transactionCaptor.getValue();
        assertThat(transaction.getAmount(), equalTo(account.getAmount()));
        assertThat(transaction.getType(), equalTo(TransactionType.IN));
        assertThat(transaction.getAccountId(), equalTo(1L));
        assertThat(transaction.getDate(), equalTo(LocalDate.now()));
        assertThat(transaction.getCategoryId(), equalTo(1L));
        ArgumentCaptor<Balance> balanceCaptor = ArgumentCaptor.forClass(Balance.class);
        verify(balanceRepository, times(2)).save(balanceCaptor.capture());
        balance = balanceCaptor.getValue();
        assertThat(balance.getYear(), equalTo(LocalDate.now().getYear()));
        assertThat(balance.getMonth(), equalTo(LocalDate.now().getMonthValue()));
        assertThat(balance.getIncoming(), equalTo(account.getAmount()));
        verify(balanceRepository).findByMonthAndYear(eq(LocalDate.now().getMonthValue()), eq(LocalDate.now().getYear()));
    }

    @Test
    public void whenSavingExistingAccount_thenNoTransactionAndBalanceCreated() {
        Account account = Account.builder().id(1L).amount(new BigDecimal("100")).build();
        when(repository.save(eq(account)))
                .thenReturn(account);

        Account result = service.save(account);

        verifyNoInteractions(categoryRepository, transactionRepository, balanceRepository);
        verify(repository).save(eq(account));

        assertThat(result, equalTo(account));
    }

    @Test
    public void whenDeleting_withExistingAccount_thenDeleted() {
        service.delete(1L);

        verify(repository).deleteById(eq(1L));
    }

    @Test
    public void whenDeleting_withNonExistingAccount_thenExceptionThrown() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(repository).deleteById(eq(1L));

        assertThrows(AccountNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    public void whenFetching_withExistingAccount_thenFetched() {
        Account account = Account.builder().build();
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(account));
        Account result = service.fetch(1L);

        verify(repository).findById(eq(1L));

        assertThat(result, equalTo(account));
    }

    @Test
    public void whenListing_thenListReturned() {
        Account account = Account.builder().build();
        when(repository.findAll())
                .thenReturn(List.of(account));

        List<Account> result = service.list();

        verify(repository).findAll();

        assertThat(result, containsInAnyOrder(account));
    }
}
