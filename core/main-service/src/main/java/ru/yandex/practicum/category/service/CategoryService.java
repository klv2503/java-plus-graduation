package ru.yandex.practicum.category.service;

import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.category.dto.GetCategoriesParams;
import ru.yandex.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(NewCategoryDto inputCat);

    void deleteCategory(long id);

    CategoryDto updateCategory(CategoryDto inputCat);

    CategoryDto getCategory(long id);

    List<CategoryDto> getCatList(GetCategoriesParams params);
}
