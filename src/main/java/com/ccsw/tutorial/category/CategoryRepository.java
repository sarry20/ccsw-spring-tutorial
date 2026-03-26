package com.ccsw.tutorial.category;

import com.ccsw.tutorial.category.model.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends CrudRepository<Category, Long> {
}
