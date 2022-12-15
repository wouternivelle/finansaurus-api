package io.nivelle.finansaurus.categories.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends PagingAndSortingRepository<Category, Long> {
    Category findCategoryByType(CategoryType type);

    List<Category> findAll();

    List<Category> findAllBySystemIsFalse();

    @Query("select case when count(c) > 0 then true else false end from Category c where c.system = true and c.id = :id")
    boolean isIncomingCategory(Long id);
}
