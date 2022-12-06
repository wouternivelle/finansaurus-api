package io.nivelle.finansaurus;

import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate.sql"})
@TestPropertySource(properties = {
        "security.username=user",
        "security.password=password"})
public abstract class CommonIntegrationTest {
    @Value("${security.username}")
    private String username;
    @Value("${security.password}")
    private String password;

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

    protected Transaction saveTransaction(Transaction transaction) {
        return getRestTemplate().postForEntity("http://localhost:" + port + "/transactions", transaction, Transaction.class)
                .getBody();
    }

    protected TestRestTemplate getRestTemplate() {
        return restTemplate.withBasicAuth(username, password);
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
