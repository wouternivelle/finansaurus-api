package io.nivelle.finansaurus.balances.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class BalanceCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private BigDecimal budgeted;
    @NotNull
    private Long categoryId;
    @NotNull
    private BigDecimal operations;
    @Transient
    private BigDecimal balance;

    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    protected BalanceCategory() {

    }

    private BalanceCategory(Builder builder) {
        id = builder.id;
        budgeted = builder.budgeted;
        categoryId = builder.categoryId;
        operations = builder.operations;
        balance = builder.balance;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
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

    public BigDecimal getBudgeted() {
        return budgeted;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getOperations() {
        return operations;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void updateBudgeted(BigDecimal budgeted) {
        this.budgeted = budgeted;
    }

    public void updateOperations(BigDecimal operations) {
        this.operations = operations;
    }

    public void updateBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public static final class Builder {
        private Long id;
        private @NotNull Long categoryId;
        private @NotNull BigDecimal budgeted = BigDecimal.ZERO;
        private @NotNull BigDecimal operations = BigDecimal.ZERO;
        private @NotNull BigDecimal balance = BigDecimal.ZERO;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder budgeted(BigDecimal budgeted) {
            this.budgeted = budgeted;
            return this;
        }

        public Builder operations(BigDecimal operations) {
            this.operations = operations;
            return this;
        }


        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public BalanceCategory build() {
            return new BalanceCategory(this);
        }
    }
}
