package io.nivelle.finansaurus.balances.adapter;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BalanceCategoryResource {
    private Long id;
    @NotNull
    private Long categoryId;
    @NotNull
    private BigDecimal budgeted;
    @NotNull
    private BigDecimal operations;
    @Transient
    private BigDecimal balance;

    protected BalanceCategoryResource() {
    }

    private BalanceCategoryResource(Builder builder) {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public BigDecimal getBudgeted() {
        return budgeted;
    }

    public BigDecimal getOperations() {
        return operations;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public static final class Builder {
        private Long id;
        private Long categoryId;
        private @NotNull BigDecimal budgeted = BigDecimal.ZERO;
        private @NotNull BigDecimal operations = BigDecimal.ZERO;
        private BigDecimal balance = BigDecimal.ZERO;

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

        public BalanceCategoryResource build() {
            return new BalanceCategoryResource(this);
        }
    }
}
