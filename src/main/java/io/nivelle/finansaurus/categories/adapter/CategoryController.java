package io.nivelle.finansaurus.categories.adapter;

import io.nivelle.finansaurus.categories.application.CategoryService;
import io.nivelle.finansaurus.categories.domain.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/categories")
public class CategoryController {
    private CategoryService service;
    private PagedResourcesAssembler<Category> pagedResourcesAssembler;
    private CategoryResourceAssembler resourceAssembler;

    @Autowired
    CategoryController(CategoryService service, PagedResourcesAssembler<Category> pagedResourcesAssembler, CategoryResourceAssembler resourceAssembler) {
        this.service = service;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.resourceAssembler = resourceAssembler;
    }

    @PostMapping
    public RepresentationModel<CategoryResource> save(@RequestBody CategoryResource resource) {
        Category category = Category.builder()
                .id(resource.getId())
                .name(resource.getName())
                .hidden(resource.isHidden())
                .system(resource.isSystem())
                .parent(resource.getParent())
                .type(resource.getType())
                .build();

        category = service.save(category);

        return resourceAssembler.toModel(category);
    }

    @GetMapping
    public CollectionModel<CategoryResource> list() {
        List<Category> categories = service.list();

        return CollectionModel.of(categories.stream().map(category -> resourceAssembler.toModel(category)).toList());
    }

    @GetMapping("no-system")
    public CollectionModel<CategoryResource> listWithoutSystem() {
        List<Category> categories = service.listWithoutSystem();

        return CollectionModel.of(categories.stream().map(category -> resourceAssembler.toModel(category)).toList());
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
}
