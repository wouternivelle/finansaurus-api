package io.nivelle.finansaurus.payees.adapter;

import io.nivelle.finansaurus.payees.application.PayeeService;
import io.nivelle.finansaurus.payees.domain.Payee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PayeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PayeeControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected PayeeService payeeService;

    @MockitoBean
    protected PayeeResourceAssembler assembler;

    @Test
    public void whenListingPayees_thenPayeesReturned() throws Exception {
        when(payeeService.list()).thenReturn(List.of(
                Payee.builder().name("Albert Heijn").build()
        ));
        when(assembler.toModel(any(Payee.class))).thenReturn(
                PayeeResource.builder()
                        .name("Albert Heijn")
                        .build()
        );

        mockMvc
                .perform(get("/payees"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.payees").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.payees[0].name").value("Albert Heijn"));
    }

    @Test
    public void whenFetchingPayee_thenPayeeReturned() throws Exception {
        when(payeeService.fetch(1L)).thenReturn(
                Payee.builder().id(1L).name("Albert Heijn").build()
        );
        when(assembler.toModel(any(Payee.class))).thenReturn(
                PayeeResource.builder()
                        .id(1L)
                        .name("Albert Heijn")
                        .build()
        );

        mockMvc
                .perform(get("/payees/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Albert Heijn"));
    }
}
