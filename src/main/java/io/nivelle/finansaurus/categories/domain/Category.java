package io.nivelle.finansaurus.categories.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean hidden;
    @NotNull
    private String name;
    private Long parent;
    private boolean system;
    @NotNull
    @Enumerated(EnumType.STRING)
    private CategoryType type;

    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;

    protected Category() {

    }

    private Category(Builder builder) {
        this.id = builder.id;
        this.hidden = builder.hidden;
        this.name = builder.name;
        this.parent = builder.parent;
        this.system = builder.system;
        this.type = builder.type;
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

    public boolean isHidden() {
        return hidden;
    }

    public String getName() {
        return name;
    }

    public Long getParent() {
        return parent;
    }

    public boolean isSystem() {
        return system;
    }

    public CategoryType getType() {
        return type;
    }

    public static final class Builder {
        private Long id;
        private boolean hidden;
        private @NotNull String name;
        private Long parent;
        private boolean system;
        private @NotNull CategoryType type;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder hidden(boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder parent(Long parent) {
            this.parent = parent;
            return this;
        }

        public Builder system(boolean system) {
            this.system = system;
            return this;
        }

        public Builder type(CategoryType type) {
            this.type = type;
            return this;
        }

        public Category build() {
            return new Category(this);
        }
    }
}
