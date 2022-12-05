package io.nivelle.finansaurus.payees.adapter;

import io.nivelle.finansaurus.payees.domain.Payee;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PayeeResourceAssembler implements RepresentationModelAssembler<Payee, PayeeResource> {
    @Override
    public PayeeResource toModel(Payee entity) {
        return PayeeResource.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastCategoryId(entity.getLastCategoryId())
                .build()
                .add(linkTo(methodOn(PayeeController.class).fetch(entity.getId())).withSelfRel());
    }
}
