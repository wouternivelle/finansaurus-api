package io.nivelle.finansaurus.categories.application;

import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImplTest {
    private CategoryService service;

    @Mock
    private CategoryRepository repository;

    @BeforeEach
    public void setup() {
        service = new CategoryServiceImpl(repository);
    }

    @Test
    public void whenSaving_thenSaved() {
        Category category = Category.builder().build();
        when(repository.save(eq(category)))
                .thenReturn(category);

        Category result = service.save(category);

        verify(repository).save(eq(category));
        assertThat(result, equalTo(category));
    }

    @Test
    public void whenDeleting_thenDeleted() {
        service.delete(1L);

        verify(repository).deleteById(eq(1L));
    }

    @Test
    public void whenListing_thenListReturned() {
        Category category = Category.builder().build();
        when(repository.findAll())
                .thenReturn(List.of(category));

        List<Category> result = service.list();

        verify(repository).findAll();
        assertThat(result, containsInAnyOrder(category));
    }

    @Test
    public void whenListingWithoutSystem_thenListReturned() {
        Category category = Category.builder().build();
        when(repository.findAllBySystemIsFalse())
                .thenReturn(List.of(category));

        List<Category> result = service.listWithoutSystem();

        verify(repository).findAllBySystemIsFalse();
        assertThat(result, containsInAnyOrder(category));
    }
}
