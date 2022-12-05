package io.nivelle.finansaurus.transactions.adapter;

import io.nivelle.finansaurus.transactions.domain.Transaction;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TransactionResourceAssembler implements RepresentationModelAssembler<Transaction, TransactionResource> {
    @Override
    public TransactionResource toModel(Transaction entity) {
        return TransactionResource.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .accountId(entity.getAccountId())
                .categoryId(entity.getCategoryId())
                .payeeId(entity.getPayeeId())
                .note(entity.getNote())
                .type(entity.getType())
                .date(entity.getDate())
                .build();
    }
}
