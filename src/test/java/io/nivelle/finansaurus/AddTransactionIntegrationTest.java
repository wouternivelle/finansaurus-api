package io.nivelle.finansaurus;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountType;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceCategoryNotFoundException;
import io.nivelle.finansaurus.balances.domain.BalanceNotFoundException;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AddTransactionIntegrationTest extends CommonIntegrationTest {

    private Account account;
    private Category category;
    private Payee payee;
    private Transaction transaction;

    @BeforeEach
    public void setup() {
        account = accountRepository.save(Account.builder().name("TEST ACCOUNT").type(AccountType.CHECKINGS).amount(new BigDecimal(3500)).build());
        category = categoryRepository.save(Category.builder().name("TEST CATEGORY").type(CategoryType.GENERAL).build());
        payee = payeeRepository.save(Payee.builder().name("TEST PAYEE").build());
        transaction = transactionRepository.save(Transaction.builder().type(TransactionType.OUT).date(LocalDate.now()).amount(new BigDecimal(100)).accountId(account.getId()).categoryId(category.getId()).payeeId(payee.getId()).build());
    }

    @Test
    void whenAddingTransactions_thenAccountAndBalanceUpdated() {
        LocalDate now = LocalDate.now();
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.OUT)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(500))
                .build()
        );
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.OUT)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(2000))
                .build()
        );
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(100))
                .build()
        );
        saveTransaction(Transaction.builder()
                .id(transaction.getId())
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(300))
                .build()
        );

        runInTransaction(() -> {
            assertThat(accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("Account should exist")).getAmount(),
                    equalTo(new BigDecimal("1500.00")));

            Balance balance = balanceRepository.findByMonthAndYear(now.getMonthValue(), now.getYear()).orElseThrow(BalanceNotFoundException::new);
            assertThat(balance.getIncoming(), equalTo(new BigDecimal("0.00")));
            BalanceCategory balanceCategory = balance.getCategories()
                    .stream()
                    .filter(cat -> cat.getCategoryId().equals(category.getId()))
                    .findAny()
                    .orElseThrow(BalanceCategoryNotFoundException::new);
            assertThat(balanceCategory.getBudgeted(), equalTo(new BigDecimal("0.00")));
            assertThat(balanceCategory.getOperations(), equalTo(new BigDecimal("2000.00")));
        });
    }

    @Test
    void whenDeletingTransactions_thenAccountAndBalanceUpdated() {
        LocalDate now = LocalDate.now();
        Transaction transaction = saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.OUT)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(500))
                .build()
        );
        deleteTransaction(transaction.getId());

        runInTransaction(() -> {
            assertThat(accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("Account should exist")).getAmount(),
                    equalTo(new BigDecimal("3500.00")));

            Balance balance = balanceRepository.findByMonthAndYear(now.getMonthValue(), now.getYear()).orElseThrow(BalanceNotFoundException::new);
            assertThat(balance.getIncoming(), equalTo(new BigDecimal("0.00")));
            BalanceCategory balanceCategory = balance.getCategories()
                    .stream()
                    .filter(cat -> cat.getCategoryId().equals(category.getId()))
                    .findAny()
                    .orElseThrow(BalanceCategoryNotFoundException::new);
            assertThat(balanceCategory.getBudgeted(), equalTo(new BigDecimal("0.00")));
            assertThat(balanceCategory.getOperations(), equalTo(new BigDecimal("0.00")));
        });
    }

    @Test
    void whenUpdatingTransaction_thenAccountAndBalanceUpdated() {
        LocalDate now = LocalDate.now();
        Transaction transaction = saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.OUT)
                .payeeName("TEST PAYEE")
                .categoryId(category.getId())
                .amount(new BigDecimal(500))
                .build()
        );

        saveTransaction(Transaction.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .accountId(transaction.getAccountId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE 2")
                .categoryId(transaction.getCategoryId())
                .amount(new BigDecimal(1000))
                .build());

        runInTransaction(() -> {
            assertThat(accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("Account should exist")).getAmount(),
                    equalTo(new BigDecimal("4500.00")));

            Balance balance = balanceRepository.findByMonthAndYear(now.getMonthValue(), now.getYear()).orElseThrow(BalanceNotFoundException::new);
            assertThat(balance.getIncoming(), equalTo(new BigDecimal("0.00")));
            BalanceCategory balanceCategory = balance.getCategories()
                    .stream()
                    .filter(cat -> cat.getCategoryId().equals(category.getId()))
                    .findAny()
                    .orElseThrow(BalanceCategoryNotFoundException::new);
            assertThat(balanceCategory.getBudgeted(), equalTo(new BigDecimal("0.00")));
            assertThat(balanceCategory.getOperations(), equalTo(new BigDecimal("-1000.00")));
        });
    }

    @Test
    void whenAddingIncomingTransactions_thenAccountAndBalanceUpdated() {
        Category incomingCurrentMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_CURRENT_MONTH);
        Category incomingNextMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_NEXT_MONTH);
        Category incomingInitial = categoryRepository.findCategoryByType(CategoryType.INITIAL);

        LocalDate now = LocalDate.now();
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE")
                .categoryId(incomingCurrentMonth.getId())
                .amount(new BigDecimal(500))
                .build()
        );
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE")
                .categoryId(incomingNextMonth.getId())
                .amount(new BigDecimal(1000))
                .build()
        );
        saveTransaction(Transaction.builder()
                .date(now)
                .accountId(account.getId())
                .type(TransactionType.IN)
                .payeeName("TEST PAYEE")
                .categoryId(incomingInitial.getId())
                .amount(new BigDecimal(1500))
                .build()
        );

        assertThat(accountRepository.findById(account.getId()).orElseThrow(() -> new IllegalStateException("Account should exist")).getAmount(),
                equalTo(new BigDecimal("6500.00")));

        Balance balance = balanceRepository.findByMonthAndYear(now.getMonthValue(), now.getYear()).orElseThrow(BalanceNotFoundException::new);
        assertThat(balance.getIncoming(), equalTo(new BigDecimal("2000.00")));
        Balance nextBalance = balanceRepository.findByMonthAndYear(now.plusMonths(1).getMonthValue(), now.plusMonths(1).getYear()).orElseThrow(BalanceNotFoundException::new);
        assertThat(nextBalance.getIncoming(), equalTo(new BigDecimal("1000.00")));
    }

}
