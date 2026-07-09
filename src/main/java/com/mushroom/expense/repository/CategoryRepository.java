package com.mushroom.expense.repository;

import com.mushroom.expense.entity.Category;
import com.mushroom.expense.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByType(TransactionType type);
}
