package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceNotFoundException;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceImplTest {
    private BalanceService service;

    @Mock
    private BalanceRepository repository;

    @BeforeEach
    public void setup() {
        service = new BalanceServiceImpl(repository);
    }

    @Test
    public void whenListing_thenBalanceOfCategoriesIsCalculated() {
        when(repository.findAllOrderByDate())
                .thenReturn(List.of(Balance.builder()
                                .month(11)
                                .year(2022)
                                .incoming(new BigDecimal("2500"))
                                .categories(List.of(BalanceCategory.builder()
                                        .categoryId(5L)
                                        .operations(new BigDecimal("250"))
                                        .budgeted(new BigDecimal("100"))
                                        .build()))
                                .build(),
                        Balance.builder()
                                .month(12)
                                .year(2022)
                                .incoming(new BigDecimal("2000"))
                                .categories(List.of(BalanceCategory.builder()
                                        .categoryId(5L)
                                        .operations(new BigDecimal("100"))
                                        .budgeted(new BigDecimal("150"))
                                        .build()))
                                .build(),
                        Balance.builder()
                                .month(1)
                                .year(2023)
                                .incoming(new BigDecimal("2300"))
                                .categories(List.of(BalanceCategory.builder()
                                        .categoryId(5L)
                                        .operations(new BigDecimal("50"))
                                        .budgeted(new BigDecimal("200"))
                                        .build()))
                                .build(),
                        Balance.builder()
                                .month(2)
                                .year(2023)
                                .incoming(new BigDecimal("2400"))
                                .build()
                ));

        List<Balance> result = service.list(2023, 1);

        assertThat(result.get(0).getCategories().get(0).getBalance(), equalTo(new BigDecimal("-150")));
        assertThat(result.get(0).getBudgeted(), equalTo(new BigDecimal("100")));
        assertThat(result.get(1).getCategories().get(0).getBalance(), equalTo(new BigDecimal("-100")));
        assertThat(result.get(2).getCategories().get(0).getBalance(), equalTo(new BigDecimal("50")));
        assertThat(result.get(3).getCategories().get(0).getBalance(), equalTo(new BigDecimal("50")));
    }


    @Test
    public void whenListing_withNoBalanceFound_thenNewBalanceSaved() {
        when(repository.findAllOrderByDate())
                .thenReturn(new ArrayList<>());
        Balance balance = Balance.builder()
                .month(2)
                .year(2023)
                .build();
        when(repository.save(any(Balance.class)))
                .thenReturn(balance);

        List<Balance> result = service.list(2023, 2);

        assertThat(result.get(0).getCategories(), hasSize(0));
        assertThat(result.get(0).getBudgeted(), equalTo(new BigDecimal("0")));
    }

    @Test
    public void whenSaving_thenSavingAndListingDone() {
        Balance balance = Balance.builder()
                .month(2)
                .year(2023)
                .incoming(new BigDecimal("2400"))
                .build();
        when(repository.save(eq(balance)))
                .thenReturn(balance);
        when(repository.findAllOrderByDate())
                .thenReturn(List.of(balance));

        Balance result = service.save(balance);

        verify(repository).save(eq(balance));
        verify(repository).findAllOrderByDate();

        assertThat(result.getYear(), equalTo(balance.getYear()));
        assertThat(result.getMonth(), equalTo(balance.getMonth()));
    }

    @Test
    public void whenUsingPreviousMonth_withPreviousMonthPresent_thenBalanceIsSaved() {
        Balance balance = Balance.builder()
                .id(1L)
                .month(2)
                .year(2023)
                .categories(List.of(
                        BalanceCategory.builder().categoryId(1L).budgeted(new BigDecimal("100")).build(),
                        BalanceCategory.builder().categoryId(2L).budgeted(new BigDecimal("200")).build()
                ))
                .build();
        Balance previousBalance = Balance.builder()
                .id(0L)
                .month(1)
                .year(2023)
                .categories(List.of(
                        BalanceCategory.builder().categoryId(2L).budgeted(new BigDecimal("300")).build()
                ))
                .build();
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(balance));
        when(repository.findByMonthAndYear(eq(1), eq(2023)))
                .thenReturn(Optional.of(previousBalance));
        when(repository.save(eq(balance)))
                .thenReturn(balance);

        Balance result = service.usePreviousMonth(1L);

        verify(repository).findById(eq(1L));
        verify(repository).save(eq(balance));
        verify(repository).findByMonthAndYear(eq(1), eq(2023));

        assertThat(result.getCategories().get(0).getBudgeted(), equalTo(new BigDecimal("100")));
        assertThat(result.getCategories().get(1).getBudgeted(), equalTo(new BigDecimal("300")));
    }

    @Test
    public void whenUsingPreviousMonth_withNoPreviousMonthPresent_thenNothingIsDone() {
        Balance balance = Balance.builder()
                .id(1L)
                .month(2)
                .year(2023)
                .categories(List.of(
                        BalanceCategory.builder().categoryId(1L).budgeted(new BigDecimal("100")).build(),
                        BalanceCategory.builder().categoryId(2L).budgeted(new BigDecimal("200")).build()
                ))
                .build();
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(balance));
        when(repository.findByMonthAndYear(eq(1), eq(2023)))
                .thenReturn(Optional.empty());

        Balance result = service.usePreviousMonth(1L);

        verify(repository).findById(eq(1L));
        verify(repository).findByMonthAndYear(eq(1), eq(2023));
        verify(repository, times(0)).save(eq(balance));

        assertThat(result, equalTo(balance));
    }

    @Test
    public void whenUsingPreviousMonth_withInCorrectId_thenExceptionThrown() {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThrows(BalanceNotFoundException.class, () -> service.usePreviousMonth(1L));
    }
}
