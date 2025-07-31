package ru.practicum.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/categories")
@Validated
@Slf4j
public class AdminCategoryController {

    public final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> addCategory(@RequestBody @Valid NewCategoryDto inputDto) {
        log.info("\nAdminCategoryController.addCategory: Request for adding of new category {}", inputDto);
        CategoryDto createdCategory = categoryService.addCategory(inputDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable
                                               @Min(value = 1, message = "ID must be positive") Long catId) {
        log.info("\nAdminCategoryController.deleteCategory: Accepted request for deleting category {}", catId);
        categoryService.deleteCategory(catId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable
                                                      @Min(value = 1, message = "ID must be positive") Long catId,
                                                      @RequestBody @Valid NewCategoryDto inputDto) {
        log.info("\nAdminCategoryController.updateCategory: Accepted request for updating category {}", catId);
        CategoryDto catDto = new CategoryDto(catId, inputDto.getName());
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.updateCategory(catDto));
    }

}
