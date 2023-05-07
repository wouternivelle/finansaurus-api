package io.nivelle.finansaurus.accounts.adapter;

import io.nivelle.finansaurus.accounts.application.AccountService;
import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected AccountService accountService;

    @MockBean
    protected AccountResourceAssembler assembler;

    @Test
    public void whenListingAccounts_thenAccountsReturned() throws Exception {
        when(accountService.list()).thenReturn(List.of(
                Account.builder().type(AccountType.CHECKINGS).build()
        ));
        when(assembler.toModel(any(Account.class))).thenReturn(AccountResource.builder().amount(new BigDecimal("3500.0")).name("Checkings Account").type(AccountType.CHECKINGS).build());

        mockMvc
                .perform(get("/accounts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.accounts").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.accounts[0].amount").value(3500))
                .andExpect(jsonPath("$._embedded.accounts[0].name").value("Checkings Account"))
                .andExpect(jsonPath("$._embedded.accounts[0].type").value("CHECKINGS"));
    }
}
