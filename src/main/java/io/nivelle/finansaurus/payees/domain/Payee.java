package io.nivelle.finansaurus.payees.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
public class Payee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    private Long lastCategoryId;

    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    private Payee(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.lastCategoryId = builder.lastCategoryId;
    }

    protected Payee() {
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

    public String getName() {
        return name;
    }

    public Long getLastCategoryId() {
        return lastCategoryId;
    }

    public static final class Builder {
        private Long id;
        private @NotNull String name;
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

        public Builder lastCategoryId(Long lastCategoryId) {
            this.lastCategoryId = lastCategoryId;
            return this;
        }

        public Payee build() {
            return new Payee(this);
        }
    }
}

