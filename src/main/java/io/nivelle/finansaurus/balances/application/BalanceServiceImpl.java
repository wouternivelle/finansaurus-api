package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import io.nivelle.finansaurus.balances.domain.BalanceNotFoundException;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import io.nivelle.finansaurus.categories.application.CategoryService;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import io.nivelle.finansaurus.transactions.domain.TransactionType;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService {
    private BalanceRepository repository;
    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @Autowired
    public BalanceServiceImpl(BalanceRepository repository, CategoryRepository categoryRepository, 
                             CategoryService categoryService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }


    @Override
    public List<Balance> list(int year, int month) {
        List<Balance> balances = repository.findAllOrderByDate();

        Optional<Balance> selectedBalance = balances
                .stream()
                .filter(balance -> balance.getMonth() == month && balance.getYear() == year)
                .findFirst();
        if (selectedBalance.isEmpty()) {
            Balance balance = repository.save(Balance.builder()
                    .month(month)
                    .year(year)
                    .build());
            balances.add(balance);
        }

        Optional<Balance> previous = Optional.empty();
        for (Balance balance : balances) {
            balance.calculateCategoryBalance(previous);
            balance.calculateBudgeted();

            previous = Optional.of(balance);
        }

        return balances;
    }

    @Override
    public Balance save(Balance balance) {
        balance = repository.save(balance);

        int year = balance.getYear();
        int month = balance.getMonth();
        return list(year, month)
                .stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month)
                .findFirst()
                .orElseThrow(BalanceNotFoundException::new);
    }

    @Override
    public Balance usePreviousMonth(long id) {
        Balance currentBalance = repository.findById(id).orElseThrow(BalanceNotFoundException::new);
        LocalDate currentMonth = LocalDate.of(currentBalance.getYear(), currentBalance.getMonth(), 1);
        LocalDate previousMonth = currentMonth.minusMonths(1);
        Optional<Balance> previousBalance = repository.findByMonthAndYear(previousMonth.getMonthValue(), previousMonth.getYear());

        if (previousBalance.isPresent()) {
            previousBalance.get().getCategories().forEach(previousCategory ->
                    currentBalance.getCategories()
                            .stream()
                            .filter(currentCategory -> currentCategory.getCategoryId().equals(previousCategory.getCategoryId()))
                            .findFirst()
                            .ifPresentOrElse(currentCategory -> currentCategory.updateBudgeted(previousCategory.getBudgeted()),
                                    () -> currentBalance.getCategories().add(BalanceCategory.builder()
                                            .categoryId(previousCategory.getCategoryId())
                                            .budgeted(previousCategory.getBudgeted())
                                            .build())));
            return repository.save(currentBalance);
        } else {
            return currentBalance;
        }
    }

    @Override
    @Transactional
    public void updateForTransactionAdded(Transaction transaction) {
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
        repository.save(balance);
    }

    @Override
    @Transactional
    public void updateForTransactionDeleted(Transaction transaction) {
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
        repository.save(balance);
    }

    private Balance findOrCreateBalance(Transaction transaction, boolean isIncomeNextMonth) {
        LocalDate date = transaction.getDate();
        if (isIncomeNextMonth) {
            date = date.plusMonths(1);
        }

        int month = date.getMonthValue();
        int year = date.getYear();

        return repository.findByMonthAndYear(month, year)
                .orElseGet(() -> repository.save(Balance.builder().month(month).year(year).build()));
    }

    private boolean isIncoming(Transaction transaction) {
        return categoryService.isIncomingCategory(transaction.getCategoryId());
    }

    private void updateCategoryOutflow(Balance balance, BigDecimal amount, Long categoryId) {
        Optional<BalanceCategory> balanceCategory = balance.getCategories().stream()
                .filter(category -> category.getCategoryId().equals(categoryId))
                .findAny();
        balanceCategory.ifPresentOrElse(
                category -> category.updateOperations(category.getOperations().add(amount)),
                () -> balance.getCategories().add(BalanceCategory.builder()
                        .categoryId(categoryId)
                        .operations(amount)
                        .build())
        );
    }
}
