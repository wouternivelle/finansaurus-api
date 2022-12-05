package io.nivelle.finansaurus.categories.adapter;

import io.nivelle.finansaurus.categories.domain.Category;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CategoryResourceAssembler implements RepresentationModelAssembler<Category, CategoryResource> {
    @Override
    public CategoryResource toModel(Category entity) {
        return CategoryResource.builder()
                .id(entity.getId())
                .name(entity.getName())
                .hidden(entity.isHidden())
                .parent(entity.getParent())
                .type(entity.getType())
                .system(entity.isSystem())
                .build();
    }
}
