package io.nivelle.finansaurus.transactions.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private BigDecimal amount;
    private Long accountId;
    private Long categoryId;
    private Long payeeId;
    @Transient
    private String payeeName;
    @NotNull
    private LocalDate date;
    private String note;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    private Transaction(Builder builder) {
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

    protected Transaction() {
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

    public void updatePayee(Long id) {
        this.payeeId = id;
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
        private Long accountId;
        private Long categoryId;
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

        public Transaction build() {
            return new Transaction(this);
        }
    }
}

