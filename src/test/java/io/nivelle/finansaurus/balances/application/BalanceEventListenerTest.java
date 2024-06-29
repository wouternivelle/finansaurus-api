package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceEventListenerTest {
    private BalanceEventListener listener;
    @Mock
    private BalanceRepository balanceRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setup() {
        listener = new BalanceEventListener(balanceRepository, categoryRepository);
    }

    @Test
    public void whenOutgoingTransactionAdded_thenBalanceOperationsIncreased() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        listener.onTransactionAdded(new TransactionAddedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().getFirst().getOperations(), equalTo(new BigDecimal("500.0")));
        assertThat(balance.getCategories().getFirst().getCategoryId(), equalTo(1L));
    }

    @Test
    public void whenIncomingTransactionAdded_thenBalanceOperationDecreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        listener.onTransactionAdded(new TransactionAddedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().getFirst().getOperations(), equalTo(new BigDecimal("-500.0")));
        assertThat(balance.getCategories().getFirst().getCategoryId(), equalTo(1L));
    }

    @Test
    public void whenIncomingNextMonthTransactionAdded_thenBalanceIncomingIncreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
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

        listener.onTransactionAdded(new TransactionAddedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getMonth(), equalTo(transaction.getDate().plusMonths(1).getMonthValue()));
        assertThat(balance.getYear(), equalTo(transaction.getDate().plusMonths(1).getYear()));
        assertThat(balance.getIncoming(), equalTo(new BigDecimal("500.0")));
    }

    @Test
    public void whenOutgoingTransactionDeleted_thenBalanceOperationsDecreased() {
        Transaction transaction = buildTransaction(TransactionType.OUT);
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        listener.onTransactionDeleted(new TransactionDeletedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().getFirst().getOperations(), equalTo(new BigDecimal("-500.0")));
        assertThat(balance.getCategories().getFirst().getCategoryId(), equalTo(1L));
    }

    @Test
    public void whenIncomingTransactionDeleted_thenBalanceOperationIncreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
        Category category = Category.builder().build();
        when(categoryRepository.findCategoryByType(eq(CategoryType.INCOME_NEXT_MONTH)))
                .thenReturn(category);
        when(categoryRepository.isIncomingCategory(eq(1L)))
                .thenReturn(false);
        Balance balance = Balance.builder().month(transaction.getDate().getMonthValue()).year(transaction.getDate().getYear()).build();
        when(balanceRepository.findByMonthAndYear(eq(transaction.getDate().getMonthValue()), eq(transaction.getDate().getYear())))
                .thenReturn(Optional.of(balance));

        listener.onTransactionDeleted(new TransactionDeletedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getCategories().getFirst().getOperations(), equalTo(new BigDecimal("500.0")));
        assertThat(balance.getCategories().getFirst().getCategoryId(), equalTo(1L));
    }

    @Test
    public void whenIncomingNextMonthTransactionDeleted_thenBalanceIncomingDecreased() {
        Transaction transaction = buildTransaction(TransactionType.IN);
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

        listener.onTransactionDeleted(new TransactionDeletedEvent(transaction));

        verify(balanceRepository).save(eq(balance));
        assertThat(balance.getMonth(), equalTo(transaction.getDate().plusMonths(1).getMonthValue()));
        assertThat(balance.getYear(), equalTo(transaction.getDate().plusMonths(1).getYear()));
        assertThat(balance.getIncoming(), equalTo(new BigDecimal("-500.0")));
    }
}
