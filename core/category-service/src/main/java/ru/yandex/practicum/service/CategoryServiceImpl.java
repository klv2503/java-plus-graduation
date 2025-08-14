package ru.yandex.practicum.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.clients.EventServiceFeign;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.GetCategoriesParams;
import ru.yandex.practicum.dto.category.NewCategoryDto;
import ru.yandex.practicum.mapper.CategoryDtoMapper;
import ru.yandex.practicum.mapper.NewCategoryMapper;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.repository.CategoryRepository;
import ru.yandex.practicum.dto.events.EventFullDto;
import ru.yandex.practicum.errors.exceptions.ForbiddenActionException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Data
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final NewCategoryMapper newCategoryMapper;

    private final EventServiceFeign eventServiceFeign;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto inputCat) {
        log.info("\nCategoryServiceImpl.addCategory {}", inputCat);
        Category category = newCategoryMapper.mapNewCategoryDtoToCategory(inputCat);
        return CategoryDtoMapper.mapCategoryToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        if (!categoryRepository.existsById(id))
            throw new EntityNotFoundException("Category with " + id + " not found");
        List<EventFullDto> events =
                eventServiceFeign.getEvents(null, null, List.of(id), null, null,0, 10).getBody();
        if (CollectionUtils.isEmpty(events)) {
            categoryRepository.deleteById(id);
        } else {
            throw new ForbiddenActionException("Cannot delete category " + id + " because it is used in one or more events");
        }
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto inputCat) {
        if (!categoryRepository.existsById(inputCat.getId()))
            throw new EntityNotFoundException("Category with " + inputCat.getId() + " not found");
        categoryRepository.save(CategoryDtoMapper.mapDtoToCategory(inputCat));
        return inputCat;
    }

    @Override
    public CategoryDto getCategory(long id) {
        return CategoryDtoMapper.mapCategoryToDto(getCategoryById(id));
    }

    @Override
    public List<CategoryDto> getCatList(GetCategoriesParams params) {
        log.info("\nAdminUserService.getAllUsers {}", params);
        int page = params.getFrom() / params.getSize();
        Pageable pageable = PageRequest.of(page, params.getSize());
        Page<Category> response = categoryRepository.findAll(pageable);
        List<Category> categories = response.getContent().stream().toList();
        return CategoryDtoMapper.mapCatListToDtoList(categories);
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category with " + id + " not found"));
    }

}
