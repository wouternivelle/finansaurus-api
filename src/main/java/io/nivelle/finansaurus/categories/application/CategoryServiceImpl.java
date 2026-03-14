package io.nivelle.finansaurus.categories.application;

import io.nivelle.finansaurus.categories.domain.Category;
import io.nivelle.finansaurus.categories.domain.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private CategoryRepository repository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Category save(Category category) {
        return repository.save(category);
    }

    @Override
    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }

    @Override
    public List<Category> list() {
        return repository.findAll();
    }

    @Override
    public List<Category> listWithoutSystem() {
        return repository.findAllBySystemIsFalse();
    }

    @Override
    public boolean isIncomingCategory(Long categoryId) {
        return repository.isIncomingCategory(categoryId);
    }
}
