package io.nivelle.finansaurus.balances.adapter;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Relation(collectionRelation = "balances")
public class BalanceResource extends RepresentationModel<BalanceResource> {
    private long id;
    @NotNull
    private BigDecimal incoming;
    @NotNull
    private BigDecimal budgeted;
    private int month;
    private int year;

    private List<BalanceCategoryResource> categories;

    protected BalanceResource() {

    }

    public BalanceResource(Builder builder) {
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

    public long getId() {
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

    public List<BalanceCategoryResource> getCategories() {
        return categories;
    }

    public static final class Builder {
        private long id;
        private BigDecimal incoming;
        private BigDecimal budgeted;
        private int month;
        private int year;
        private List<BalanceCategoryResource> categories = new ArrayList<>();

        private Builder() {
        }

        public Builder id(long id) {
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

        public Builder categories(List<BalanceCategoryResource> categories) {
            this.categories = categories;
            return this;
        }

        public BalanceResource build() {
            return new BalanceResource(this);
        }
    }
}
