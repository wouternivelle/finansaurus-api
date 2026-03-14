package io.nivelle.finansaurus.categories.adapter;

import io.nivelle.finansaurus.categories.application.CategoryService;
import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected CategoryService categoryService;

    @MockitoBean
    protected CategoryResourceAssembler assembler;

    @Test
    public void whenListingCategories_thenCategoriesReturned() throws Exception {
        when(categoryService.list()).thenReturn(List.of(
                Category.builder().name("Groceries").type(CategoryType.GENERAL).build()
        ));
        when(assembler.toModel(any(Category.class))).thenReturn(
                CategoryResource.builder()
                        .name("Groceries")
                        .type(CategoryType.GENERAL)
                        .build()
        );

        mockMvc
                .perform(get("/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.categories").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.categories[0].name").value("Groceries"))
                .andExpect(jsonPath("$._embedded.categories[0].type").value("GENERAL"));
    }

    @Test
    public void whenListingCategoriesWithoutSystem_thenNonSystemCategoriesReturned() throws Exception {
        when(categoryService.listWithoutSystem()).thenReturn(List.of(
                Category.builder().name("Groceries").type(CategoryType.GENERAL).build()
        ));
        when(assembler.toModel(any(Category.class))).thenReturn(
                CategoryResource.builder()
                        .name("Groceries")
                        .type(CategoryType.GENERAL)
                        .build()
        );

        mockMvc
                .perform(get("/categories/no-system"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(jsonPath("$._embedded.categories").value(hasSize(1)))
                .andExpect(jsonPath("$._embedded.categories[0].name").value("Groceries"));
    }
}
