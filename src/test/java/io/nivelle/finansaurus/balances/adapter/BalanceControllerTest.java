package io.nivelle.finansaurus.balances.adapter;

import io.nivelle.finansaurus.balances.application.BalanceService;
import io.nivelle.finansaurus.balances.domain.Balance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BalanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BalanceControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected BalanceService balanceService;

    @MockitoBean
    protected BalanceResourceAssembler assembler;

    @Test
    public void whenListingBalances_thenBalancesReturned() throws Exception {
        when(balanceService.list(2024, 1)).thenReturn(List.of(
                Balance.builder().month(1).year(2024).incoming(new BigDecimal("1000.00")).build()
        ));
        when(assembler.toModel(any(Balance.class))).thenReturn(
                BalanceResource.builder()
                        .month(1)
                        .year(2024)
                        .incoming(new BigDecimal("1000.00"))
                        .budgeted(new BigDecimal("800.00"))
                        .categories(List.of())
                        .build()
        );

        mockMvc
                .perform(get("/balances/2024/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.balances").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.balances[0].month").value(1))
                .andExpect(jsonPath("$._embedded.balances[0].year").value(2024))
                .andExpect(jsonPath("$._embedded.balances[0].incoming").value(1000.00));
    }
}
