package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;

@Component
public class NewCategoryMapper {

    public Category mapNewCategoryDtoToCategory(NewCategoryDto inpurDto) {
        Category category = new Category();
        category.setName(inpurDto.getName());
        return category;
    }
}
