package io.nivelle.finansaurus.transactions.adapter;

import io.nivelle.finansaurus.transactions.application.TransactionService;
import io.nivelle.finansaurus.transactions.domain.PeriodicalReport;
import io.nivelle.finansaurus.transactions.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/transactions")
public class TransactionController {
    private final TransactionService service;
    private final PagedResourcesAssembler<Transaction> pagedResourcesAssembler;
    private final TransactionResourceAssembler resourceAssembler;
    private final PeriodicalReportResourceAssembler periodicalReportResourceAssembler;

    @Autowired
    TransactionController(TransactionService service, PagedResourcesAssembler<Transaction> pagedResourcesAssembler, TransactionResourceAssembler resourceAssembler, PeriodicalReportResourceAssembler periodicalReportResourceAssembler) {
        this.service = service;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.resourceAssembler = resourceAssembler;
        this.periodicalReportResourceAssembler = periodicalReportResourceAssembler;
    }

    @PostMapping
    public RepresentationModel<TransactionResource> save(@RequestBody TransactionResource resource) {
        Transaction transaction = Transaction.builder()
                .id(resource.getId())
                .amount(resource.getAmount())
                .accountId(resource.getAccountId())
                .categoryId(resource.getCategoryId())
                .payeeName(resource.getPayeeName())
                .date(resource.getDate())
                .note(resource.getNote())
                .type(resource.getType())
                .build();

        transaction = service.save(transaction);

        return resourceAssembler.toModel(transaction);
    }

    @GetMapping("{id}")
    public RepresentationModel<TransactionResource> fetch(@PathVariable(name = "id") long id) {
        return resourceAssembler.toModel(service.fetch(id));
    }

    @GetMapping
    public PagedModel<TransactionResource> list(Pageable pageable) {
        Page<Transaction> transactions = service.list(pageable);

        return pagedResourcesAssembler.toModel(transactions, resourceAssembler);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") long id) {
        try {
            service.delete(id);

            return ResponseEntity.noContent().build();
        } catch (EmptyResultDataAccessException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("incoming-for-balance/{year}/{month}")
    public CollectionModel<TransactionResource> list(@PathVariable("year") Integer year, @PathVariable("month") Integer month) {
        List<Transaction> transactions = service.listIncomingForBalance(year, month);

        return CollectionModel.of(transactions.stream().map(resourceAssembler::toModel).toList());
    }

    @GetMapping("list/{year}/{month}/{category}")
    public CollectionModel<TransactionResource> listForMonthAndCategory(@PathVariable("year") Integer year, @PathVariable("month") Integer month, @PathVariable("category") Long categoryId) {
        List<Transaction> transactions = service.listForMonthAndCategory(month, year, categoryId);

        return CollectionModel.of(transactions.stream().map(resourceAssembler::toModel).toList());
    }

    @GetMapping("reports/out")
    public CollectionModel<PeriodicalReportResource> listForMonthAndCategory(@RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<PeriodicalReport> reports = service.reportOutgoingForPeriod(start, end);

        return CollectionModel.of(reports.stream().map(periodicalReportResourceAssembler::toModel).toList());
    }
}
