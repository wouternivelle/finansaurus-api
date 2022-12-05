package io.nivelle.finansaurus.payees.adapter;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;

@Relation(collectionRelation = "payees")
public class PayeeResource extends RepresentationModel<PayeeResource> {
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String owner;
    private Long lastCategoryId;

    protected PayeeResource() {

    }

    private PayeeResource(Builder builder) {
        id = builder.id;
        name = builder.name;
        owner = builder.owner;
        lastCategoryId = builder.lastCategoryId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public Long getLastCategoryId() {
        return lastCategoryId;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private @NotNull String name;
        private @NotNull String owner;
        private Long lastCategoryId;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder lastCategoryId(Long lastCategoryId) {
            this.lastCategoryId = lastCategoryId;
            return this;
        }

        public PayeeResource build() {
            return new PayeeResource(this);
        }
    }
}
