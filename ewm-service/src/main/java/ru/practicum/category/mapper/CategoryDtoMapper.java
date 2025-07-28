package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

import java.util.List;

@Component
public class CategoryDtoMapper {
    public static CategoryDto mapCategoryToDto(Category cat) {
        return new CategoryDto(cat.getId(), cat.getName());
    }

    public static Category mapDtoToCategory(CategoryDto catDto) {
        return new Category(catDto.getId(), catDto.getName());
    }

    public static List<CategoryDto> mapCatListToDtoList(List<Category> cats) {
        return cats.stream()
                .map(CategoryDtoMapper::mapCategoryToDto)
                .toList();
    }
}
