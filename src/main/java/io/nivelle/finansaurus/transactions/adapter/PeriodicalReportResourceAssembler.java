package io.nivelle.finansaurus.transactions.adapter;

import io.nivelle.finansaurus.transactions.domain.PeriodicalReport;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PeriodicalReportResourceAssembler implements RepresentationModelAssembler<PeriodicalReport, PeriodicalReportResource> {
    @Override
    public PeriodicalReportResource toModel(PeriodicalReport entity) {
        return PeriodicalReportResource.builder()
                .amount(entity.getAmount())
                .categoryName(entity.getCategoryName())
                .build();
    }
}
