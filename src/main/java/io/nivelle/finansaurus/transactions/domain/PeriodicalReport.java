package io.nivelle.finansaurus.transactions.domain;

import java.math.BigDecimal;

public class PeriodicalReport {
    private BigDecimal amount;
    private String categoryName;

    public PeriodicalReport(BigDecimal amount, String categoryName) {
        this.amount = amount;
        this.categoryName = categoryName;
    }

    public PeriodicalReport(Builder builder) {
        this.amount = builder.amount;
        this.categoryName = builder.categoryName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BigDecimal amount;
        private String categoryName;

        private Builder() {
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public PeriodicalReport build() {
            return new PeriodicalReport(this);
        }
    }
}
