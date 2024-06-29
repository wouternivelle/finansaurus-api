package io.nivelle.finansaurus.common.domain;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
