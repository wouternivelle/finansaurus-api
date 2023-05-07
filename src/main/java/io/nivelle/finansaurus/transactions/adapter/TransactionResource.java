package io.nivelle.finansaurus.transactions.adapter;

import io.nivelle.finansaurus.transactions.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Relation(collectionRelation = "transactions")
public class TransactionResource extends RepresentationModel<TransactionResource> {
    private Long id;

    @NotNull
    private BigDecimal amount;
    @NotNull
    private Long accountId;
    @NotNull
    private Long categoryId;
    private Long payeeId;
    private String payeeName;
    @NotNull
    private LocalDate date;
    private String note;
    @NotNull
    private TransactionType type;

    private TransactionResource(Builder builder) {
        this.id = builder.id;
        this.amount = builder.amount;
        this.accountId = builder.accountId;
        this.payeeId = builder.payeeId;
        this.payeeName = builder.payeeName;
        this.categoryId = builder.categoryId;
        this.date = builder.date;
        this.note = builder.note;
        this.type = builder.type;
    }

    protected TransactionResource() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getPayeeId() {
        return payeeId;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public TransactionType getType() {
        return type;
    }

    public static final class Builder {
        private Long id;
        private @NotNull BigDecimal amount;
        private @NotNull Long accountId;
        private @NotNull Long categoryId;
        private Long payeeId;
        private String payeeName;
        private @NotNull LocalDate date;
        private String note;
        private TransactionType type;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder accountId(Long accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder payeeId(Long payeeId) {
            this.payeeId = payeeId;
            return this;
        }

        public Builder payeeName(String payeeName) {
            this.payeeName = payeeName;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public TransactionResource build() {
            return new TransactionResource(this);
        }
    }
}

