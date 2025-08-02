package ru.practicum.category.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.GetCategoriesParams;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryDtoMapper;
import ru.practicum.category.mapper.NewCategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final NewCategoryMapper newCategoryMapper;

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
        categoryRepository.deleteById(id);
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
