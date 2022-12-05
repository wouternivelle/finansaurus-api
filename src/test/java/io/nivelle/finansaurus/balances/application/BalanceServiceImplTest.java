package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

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
    public void whenListingTransactions_thenBalanceOfCategoriesIsCalculated() {
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
}
