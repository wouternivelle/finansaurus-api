package io.nivelle.finansaurus.transactions.adapter;

import io.nivelle.finansaurus.transactions.application.TransactionService;
import io.nivelle.finansaurus.transactions.domain.PeriodicalReport;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TransactionControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected TransactionService transactionService;

    @MockitoBean
    protected TransactionResourceAssembler assembler;

    @MockitoBean
    protected PeriodicalReportResourceAssembler periodicalReportResourceAssembler;

    @MockitoBean
    protected PagedResourcesAssembler<Transaction> pagedResourcesAssembler;

    @Test
    public void whenFetchingTransaction_thenTransactionReturned() throws Exception {
        when(transactionService.fetch(1L)).thenReturn(
                Transaction.builder().id(1L).amount(new BigDecimal("50.00")).type(TransactionType.OUT).build()
        );
        when(assembler.toModel(any(Transaction.class))).thenReturn(
                TransactionResource.builder()
                        .id(1L)
                        .amount(new BigDecimal("50.00"))
                        .type(TransactionType.OUT)
                        .accountId(1L)
                        .categoryId(1L)
                        .date(LocalDate.of(2024, 1, 15))
                        .build()
        );

        mockMvc
                .perform(get("/transactions/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.type").value("OUT"));
    }

    @Test
    public void whenListingTransactionsForMonthAndCategory_thenTransactionsReturned() throws Exception {
        when(transactionService.listForMonthAndCategory(1, 2024, 5L)).thenReturn(List.of(
                Transaction.builder().amount(new BigDecimal("25.00")).type(TransactionType.OUT).build()
        ));
        when(assembler.toModel(any(Transaction.class))).thenReturn(
                TransactionResource.builder()
                        .amount(new BigDecimal("25.00"))
                        .type(TransactionType.OUT)
                        .accountId(1L)
                        .categoryId(5L)
                        .date(LocalDate.of(2024, 1, 10))
                        .build()
        );

        mockMvc
                .perform(get("/transactions/list/2024/1/5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.transactions").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.transactions[0].amount").value(25.00))
                .andExpect(jsonPath("$._embedded.transactions[0].type").value("OUT"));
    }

    @Test
    public void whenListingReports_thenReportsReturned() throws Exception {
        when(transactionService.reportOutgoingForPeriod(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(
                PeriodicalReport.builder().categoryName("Groceries").amount(new BigDecimal("300.00")).build()
        ));
        when(periodicalReportResourceAssembler.toModel(any(PeriodicalReport.class))).thenReturn(
                PeriodicalReportResource.builder()
                        .categoryName("Groceries")
                        .amount(new BigDecimal("300.00"))
                        .build()
        );

        mockMvc
                .perform(get("/transactions/reports/out?start=2024-01-01&end=2024-01-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.reports").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.reports[0].categoryName").value("Groceries"))
                .andExpect(jsonPath("$._embedded.reports[0].amount").value(300.00));
    }
}
