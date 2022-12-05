package io.nivelle.finansaurus.balances.application;

import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceNotFoundException;
import io.nivelle.finansaurus.balances.domain.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService {
    private BalanceRepository repository;

    @Autowired
    public BalanceServiceImpl(BalanceRepository repository) {
        this.repository = repository;
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
        Optional<Balance> previousBalance = repository.findByMonthAndYear(previousMonth.getYear(), previousMonth.getMonthValue());

        if (previousBalance.isPresent()) {
            currentBalance.getCategories().forEach(category -> {
                previousBalance.get().getCategories()
                        .stream()
                        .filter(previousCategory -> previousCategory.getCategoryId().equals(category.getCategoryId()))
                        .findFirst()
                        .ifPresent(previousCategory -> category.updateBudgeted(previousCategory.getBudgeted()));
            });
            return repository.save(currentBalance);
        } else {
            return currentBalance;
        }
    }
}
