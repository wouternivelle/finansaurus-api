package io.nivelle.finansaurus.transactions.domain.event;

import io.nivelle.finansaurus.common.domain.DomainEvent;
import io.nivelle.finansaurus.transactions.domain.Transaction;

public record TransactionDeletedEvent(Transaction transaction) implements DomainEvent {
}
