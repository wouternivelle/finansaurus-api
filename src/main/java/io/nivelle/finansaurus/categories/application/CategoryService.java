package io.nivelle.finansaurus.categories.application;

import io.nivelle.finansaurus.categories.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Category save(Category category);

    void delete(long id);

    Page<Category> list(Pageable pageable);

    List<Category> listWithoutSystem();
}
