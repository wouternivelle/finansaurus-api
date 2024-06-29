package io.nivelle.finansaurus.accounts.domain.event;

import io.nivelle.finansaurus.accounts.domain.Account;
import io.nivelle.finansaurus.common.domain.DomainEvent;

public record AccountSavedEvent(Account account, boolean isNew) implements DomainEvent {
}
