package io.nivelle.finansaurus.categories.application;

import io.nivelle.finansaurus.categories.domain.Category;

import java.util.List;

public interface CategoryService {
    Category save(Category category);

    void delete(long id);

    List<Category> list();

    List<Category> listWithoutSystem();
}
