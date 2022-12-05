package io.nivelle.finansaurus.payees.adapter;

import io.nivelle.finansaurus.payees.application.PayeeService;
import io.nivelle.finansaurus.payees.domain.Payee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/payees")
public class PayeeController {
    private PayeeService service;
    private PagedResourcesAssembler<Payee> pagedResourcesAssembler;
    private PayeeResourceAssembler resourceAssembler;

    @Autowired
    PayeeController(PayeeService service, PagedResourcesAssembler<Payee> pagedResourcesAssembler, PayeeResourceAssembler resourceAssembler) {
        this.service = service;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.resourceAssembler = resourceAssembler;
    }

    @PostMapping
    public RepresentationModel<PayeeResource> save(@RequestBody PayeeResource resource) {
        Payee payee = Payee.builder().id(resource.getId()).name(resource.getName()).lastCategoryId(resource.getLastCategoryId()).build();

        payee = service.save(payee);

        return resourceAssembler.toModel(payee);
    }

    @GetMapping
    public PagedModel<PayeeResource> list(Pageable pageable) {
        Page<Payee> payees = service.list(pageable);

        return pagedResourcesAssembler.toModel(payees, resourceAssembler);
    }

    @GetMapping("{id}")
    public RepresentationModel<PayeeResource> fetch(@PathVariable(name = "id") long id) {
        return resourceAssembler.toModel(service.fetch(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") long id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
