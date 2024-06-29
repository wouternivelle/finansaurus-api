package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.accounts.domain.AccountNotFoundException;
import io.nivelle.finansaurus.accounts.domain.AccountRepository;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import io.nivelle.finansaurus.transactions.domain.event.TransactionAddedEvent;
import io.nivelle.finansaurus.transactions.domain.event.TransactionDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Component
public class BalanceEventListener {
    private final BalanceRepository balanceRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public BalanceEventListener(BalanceRepository balanceRepository, CategoryRepository categoryRepository) {
        this.balanceRepository = balanceRepository;
        this.categoryRepository = categoryRepository;
    }

    @EventListener
    public void onTransactionAdded(TransactionAddedEvent event) {
        Transaction transaction = event.transaction();
        Category incomeNextMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_NEXT_MONTH);
        boolean isIncomeNextMonth = transaction.getCategoryId().equals(incomeNextMonth.getId());
        Balance balance = findOrCreateBalance(transaction, isIncomeNextMonth);

        if (isIncoming(transaction)) {
            balance.updateIncoming(balance.getIncoming().add(transaction.getAmount()));
        } else {
            BigDecimal amount;
            if (TransactionType.IN.equals(transaction.getType())) {
                amount = transaction.getAmount().negate();
            } else {
                amount = transaction.getAmount();
            }
            updateCategoryOutflow(balance, amount, transaction.getCategoryId());
        }
        balanceRepository.save(balance);
    }

    @EventListener
    public void onTransactionDeleted(TransactionDeletedEvent event) {
        Transaction transaction = event.transaction();
        Category incomeNextMonth = categoryRepository.findCategoryByType(CategoryType.INCOME_NEXT_MONTH);
        boolean isIncomeNextMonth = transaction.getCategoryId().equals(incomeNextMonth.getId());
        Balance balance = findOrCreateBalance(transaction, isIncomeNextMonth);
        if (isIncoming(transaction)) {
            balance.updateIncoming(balance.getIncoming().subtract(transaction.getAmount()));
        } else {
            BigDecimal amount;
            if (TransactionType.IN.equals(transaction.getType())) {
                amount = transaction.getAmount();
            } else {
                amount = transaction.getAmount().negate();
            }
            updateCategoryOutflow(balance, amount, transaction.getCategoryId());
        }
        balanceRepository.save(balance);
    }

    private Balance findOrCreateBalance(Transaction transaction, boolean isIncomeNextMonth) {
        LocalDate date = transaction.getDate();
        if (isIncomeNextMonth) {
            date = date.plusMonths(1);
        }

        int month = date.getMonthValue();
        int year = date.getYear();

        return balanceRepository.findByMonthAndYear(month, year)
                .orElseGet(() -> balanceRepository.save(Balance.builder().month(month).year(year).build()));
    }

    private boolean isIncoming(Transaction transaction) {
        return categoryRepository.isIncomingCategory(transaction.getCategoryId());
    }

    private void updateCategoryOutflow(Balance balance, BigDecimal amount, Long categoryId) {
        Optional<BalanceCategory> balanceCategory = balance.getCategories().stream()
                .filter(category -> category.getCategoryId().equals(categoryId))
                .findAny();
        balanceCategory.ifPresentOrElse(category -> category.updateOperations(category.getOperations().add(amount)),
                () -> balance.getCategories().add(BalanceCategory.builder().categoryId(categoryId).operations(amount).build()));
    }
}
