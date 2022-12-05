package io.nivelle.finansaurus.balances.domain;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private BigDecimal incoming;
    @Transient
    private BigDecimal budgeted;
    private int month;
    private int year;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "balance_id")
    private List<BalanceCategory> categories;

    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    protected Balance() {

    }

    public Balance(Builder builder) {
        id = builder.id;
        incoming = builder.incoming;
        budgeted = builder.budgeted;
        month = builder.month;
        year = builder.year;
        categories = builder.categories;
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    void onCreate() {
        creationTime = LocalDateTime.now();
        modificationTime = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        modificationTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getIncoming() {
        return incoming;
    }

    public BigDecimal getBudgeted() {
        return budgeted;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public List<BalanceCategory> getCategories() {
        return categories;
    }

    public void updateIncoming(BigDecimal incoming) {
        this.incoming = incoming;
    }

    public void calculateBudgeted() {
        this.budgeted = categories.stream().map(BalanceCategory::getBudgeted).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void calculateCategoryBalance(Optional<Balance> previousBalance) {
        categories.forEach(category -> category.updateBalance(category.getBudgeted().subtract(category.getOperations())));

        previousBalance.ifPresent(previous ->
                previous.getCategories().forEach(previousCategory -> {
                    Optional<BalanceCategory> foundCategory = categories
                            .stream()
                            .filter(category -> category.getCategoryId().equals(previousCategory.getCategoryId()))
                            .findFirst();
                    foundCategory.ifPresentOrElse(category -> category.updateBalance(category.getBalance().add(previousCategory.getBalance())),
                            () -> categories.add(BalanceCategory.builder()
                                    .categoryId(previousCategory.getCategoryId())
                                    .balance(previousCategory.getBalance())
                                    .build()));
                })
        );
    }

    public static final class Builder {
        private Long id;
        private @NotNull BigDecimal incoming = BigDecimal.ZERO;
        private BigDecimal budgeted = BigDecimal.ZERO;
        private @NotNull int month;
        private @NotNull int year;
        private List<BalanceCategory> categories = new ArrayList<>();

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder incoming(BigDecimal incoming) {
            this.incoming = incoming;
            return this;
        }

        public Builder budgeted(BigDecimal budgeted) {
            this.budgeted = budgeted;
            return this;
        }

        public Builder month(int month) {
            this.month = month;
            return this;
        }

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder categories(List<BalanceCategory> categories) {
            this.categories = categories;
            return this;
        }

        public Balance build() {
            return new Balance(this);
        }
    }
}
