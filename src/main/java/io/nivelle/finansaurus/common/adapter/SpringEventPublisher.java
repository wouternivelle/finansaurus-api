package io.nivelle.finansaurus.common.adapter;

import io.nivelle.finansaurus.common.domain.DomainEvent;
import io.nivelle.finansaurus.common.domain.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringEventPublisher.class);

    @Autowired
    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent domainEvent) {
        applicationEventPublisher.publishEvent(domainEvent);
    }
}
