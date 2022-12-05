package io.nivelle.finansaurus.balances.adapter;

import io.nivelle.finansaurus.balances.application.BalanceService;
import io.nivelle.finansaurus.balances.domain.Balance;
import io.nivelle.finansaurus.balances.domain.BalanceCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/balances")
public class BalanceController {
    private BalanceService service;
    private BalanceResourceAssembler resourceAssembler;

    @Autowired
    BalanceController(BalanceService service, BalanceResourceAssembler resourceAssembler) {
        this.service = service;
        this.resourceAssembler = resourceAssembler;
    }

    @GetMapping("{year}/{month}")
    public CollectionModel<BalanceResource> list(@PathVariable("year") Integer year, @PathVariable("month") Integer month) {
        List<Balance> balances = service.list(year, month);

        return CollectionModel.of(balances.stream().map(balance -> resourceAssembler.toModel(balance)).toList());
    }

    @PostMapping
    public RepresentationModel<BalanceResource> save(@RequestBody BalanceResource resource) {
        Balance balance = Balance.builder()
                .id(resource.getId())
                .year(resource.getYear())
                .month(resource.getMonth())
                .incoming(resource.getIncoming())
                .categories(resource.getCategories().stream()
                        .map(category -> BalanceCategory.builder()
                                .operations(category.getOperations())
                                .budgeted(category.getBudgeted())
                                .categoryId(category.getCategoryId())
                                .id(category.getId())
                                .build()
                        ).toList())
                .build();

        balance = service.save(balance);

        return resourceAssembler.toModel(balance);
    }

    @PatchMapping("use-previous-month")
    public RepresentationModel<BalanceResource> usePreviousMonth(@RequestBody BalanceResource resource) {
        Balance balance = service.usePreviousMonth(resource.getId());

        return resourceAssembler.toModel(balance);
    }
}
