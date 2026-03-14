package io.nivelle.finansaurus.accounts.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.application.BalanceService;
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
import static org.mockito.ArgumentMatchers.any;
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
    private BalanceService balanceService;

    @BeforeEach
    public void setup() {
        service = new AccountServiceImpl(repository, transactionRepository, categoryRepository, balanceService);
    }

    @Test
    public void whenSavingNewAccount_thenAccountAndInitialTransactionCreated() {
        Account account = Account.builder().amount(new BigDecimal("100")).build();
        when(repository.save(eq(account)))
                .thenReturn(Account.builder().id(1L).amount(new BigDecimal("100")).build());
        when(categoryRepository.findCategoryByType(eq(CategoryType.INITIAL)))
                .thenReturn(Category.builder().id(1L).build());
        Transaction transaction = Transaction.builder()
                .accountId(1L)
                .type(TransactionType.IN)
                .amount(account.getAmount())
                .categoryId(1L)
                .date(LocalDate.now())
                .build();
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

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
        verify(balanceService).updateForTransactionAdded(any(Transaction.class));
    }

    @Test
    public void whenSavingExistingAccount_thenNoTransactionAndBalanceCreated() {
        Account account = Account.builder().id(1L).amount(new BigDecimal("100")).build();
        when(repository.save(eq(account)))
                .thenReturn(account);

        Account result = service.save(account);

        verifyNoInteractions(categoryRepository, transactionRepository, balanceService);
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
