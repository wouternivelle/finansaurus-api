package io.nivelle.finansaurus.accounts.domain;


import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String name;
    private boolean starred;
    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountType type;

    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    protected Account() {

    }

    public Account(Builder builder) {
        id = builder.id;
        name = builder.name;
        starred = builder.starred;
        amount = builder.amount;
        type = builder.type;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public boolean isStarred() {
        return starred;
    }

    public AccountType getType() {
        return type;
    }

    public void updateAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public static final class Builder {
        private Long id;
        @NotNull
        private BigDecimal amount = BigDecimal.ZERO;
        @NotNull
        private String name;
        private boolean starred;
        @NotNull
        private AccountType type;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder starred(boolean starred) {
            this.starred = starred;
            return this;
        }

        public Builder type(AccountType type) {
            this.type = type;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
