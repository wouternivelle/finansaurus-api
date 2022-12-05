package io.nivelle.finansaurus.balances.adapter;

import io.nivelle.finansaurus.balances.domain.Balance;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BalanceResourceAssembler implements RepresentationModelAssembler<Balance, BalanceResource> {
    @Override
    public BalanceResource toModel(Balance entity) {
        return BalanceResource.builder()
                .id(entity.getId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .incoming(entity.getIncoming())
                .budgeted(entity.getBudgeted())
                .categories(entity.getCategories().stream().map(balanceCategory -> BalanceCategoryResource.builder()
                        .id(balanceCategory.getId())
                        .categoryId(balanceCategory.getCategoryId())
                        .budgeted(balanceCategory.getBudgeted())
                        .operations(balanceCategory.getOperations())
                        .balance(balanceCategory.getBalance())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
