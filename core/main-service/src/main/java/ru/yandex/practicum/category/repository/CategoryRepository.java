package ru.yandex.practicum.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.category.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}