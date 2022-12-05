package io.nivelle.finansaurus.common;

public class NotFoundException extends RuntimeException {
    private Long id;

    public NotFoundException(Long id) {
        this.id = id;
    }

    public NotFoundException() {
    }

    public Long getId() {
        return id;
    }
}
