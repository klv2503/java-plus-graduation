package ru.yandex.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.model.Category;

@Component
public class NewCategoryMapper {

    public Category mapNewCategoryDtoToCategory(NewCategoryDto inpurDto) {
        Category category = new Category();
        category.setName(inpurDto.getName());
        return category;
    }
}
