package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.category.NewCategoryDto;
import ru.yandex.practicum.model.Category;

@Component
public class NewCategoryMapper {

    public Category mapNewCategoryDtoToCategory(NewCategoryDto inpurDto) {
        Category category = new Category();
        category.setName(inpurDto.getName());
        return category;
    }
}
