package io.nivelle.finansaurus.transactions.adapter;

import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "reports")
public class PeriodicalReportResource extends RepresentationModel<PeriodicalReportResource> {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String categoryName;

    private PeriodicalReportResource(Builder builder) {
        this.amount = builder.amount;
        this.categoryName = builder.categoryName;
    }

    protected PeriodicalReportResource() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static final class Builder {
        @NotNull
        private BigDecimal amount;
        @NotNull
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

        public PeriodicalReportResource build() {
            return new PeriodicalReportResource(this);
        }
    }
}

