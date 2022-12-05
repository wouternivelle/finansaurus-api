package io.nivelle.finansaurus.accounts.adapter;

import io.nivelle.finansaurus.accounts.application.AccountService;
import io.nivelle.finansaurus.accounts.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/accounts")
public class AccountController {
    private AccountService service;
    private PagedResourcesAssembler<Account> pagedResourcesAssembler;
    private AccountResourceAssembler resourceAssembler;

    @Autowired
    AccountController(AccountService service, PagedResourcesAssembler<Account> pagedResourcesAssembler, AccountResourceAssembler resourceAssembler) {
        this.service = service;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.resourceAssembler = resourceAssembler;
    }

    @PostMapping
    public RepresentationModel<AccountResource> save(@RequestBody AccountResource resource) {
        Account account = Account.builder().id(resource.getId()).name(resource.getName()).type(resource.getType()).amount(resource.getAmount()).starred(resource.isStarred()).build();

        account = service.save(account);

        return resourceAssembler.toModel(account);
    }

    @GetMapping
    public PagedModel<AccountResource> list(Pageable pageable) {
        Page<Account> accounts = service.list(pageable);

        return pagedResourcesAssembler.toModel(accounts, resourceAssembler);
    }

    @GetMapping("{id}")
    public RepresentationModel<AccountResource> fetch(@PathVariable(name = "id") long id) {
        return resourceAssembler.toModel(service.fetch(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") long id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}
