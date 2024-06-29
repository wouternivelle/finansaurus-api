package io.nivelle.finansaurus;

import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate.sql"})
@ActiveProfiles("test")
public abstract class CommonIntegrationTest {
    @LocalServerPort
    protected int port;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected TransactionTemplate transactionTemplate;
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected AccountRepository accountRepository;
    @Autowired
    protected CategoryRepository categoryRepository;
    @Autowired
    protected PayeeRepository payeeRepository;
    @Autowired
    protected TransactionRepository transactionRepository;
    @Autowired
    protected BalanceRepository balanceRepository;
    @MockBean
    private JwtDecoder jwtDecoder;

    protected Transaction saveTransaction(Transaction transaction) {
        return restTemplate.postForEntity("http://localhost:" + port + "/transactions", transaction, Transaction.class)
                .getBody();
    }

    protected void deleteTransaction(Long id) {
        restTemplate.delete("http://localhost:" + port + "/transactions/" + id);
    }

    protected void runInTransaction(Runnable runnable) {
        transactionTemplate.execute(status -> {
            entityManager.flush();
            entityManager.clear();

            try {
                runnable.run();
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new IllegalStateException(e);
            }
            return null;
        });
    }
}
