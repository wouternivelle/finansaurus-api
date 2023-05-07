package io.nivelle.finansaurus.accounts.adapter;

import io.nivelle.finansaurus.accounts.domain.AccountType;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "accounts")
public class AccountResource extends RepresentationModel<AccountResource> {
    private Long id;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String name;
    private boolean starred;
    @NotNull
    private AccountType type;

    protected AccountResource() {

    }

    private AccountResource(Builder builder) {
        id = builder.id;
        amount = builder.amount;
        name = builder.name;
        starred = builder.starred;
        type = builder.type;
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

    public String getName() {
        return name;
    }

    public boolean isStarred() {
        return starred;
    }

    public AccountType getType() {
        return type;
    }

    public static final class Builder {
        private Long id;
        @NotNull
        private BigDecimal amount;
        @NotNull
        private String name;
        private boolean starred;
        @NotNull
        private AccountType type;

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

        public AccountResource build() {
            return new AccountResource(this);
        }
    }
}
