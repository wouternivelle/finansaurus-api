package io.nivelle.finansaurus.accounts.adapter;

import io.nivelle.finansaurus.accounts.domain.Account;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AccountResourceAssembler implements RepresentationModelAssembler<Account, AccountResource> {
    @Override
    public AccountResource toModel(Account entity) {
        return AccountResource.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .name(entity.getName())
                .type(entity.getType())
                .starred(entity.isStarred())
                .build()
                .add(linkTo(methodOn(AccountController.class).fetch(entity.getId())).withSelfRel());
    }
}
