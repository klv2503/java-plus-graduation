package ru.practicum.category.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.GetCategoriesParams;
import ru.practicum.category.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
@Validated
@Slf4j
public class PublicCategoryController {

    public final CategoryService categoryService;

    @GetMapping("/{catId}")
    public ResponseEntity<CategoryDto> getInfoById(@PathVariable
                                                   @Min(value = 1, message = "ID must be positive") Long catId) {
        log.info("\nPublicCategoryController: Accepted request for getting category {}", catId);
        CategoryDto cat = categoryService.getCategory(catId);
        return ResponseEntity.status(HttpStatus.OK).body(cat);
    }

    //в спецификации упоминаются фильтры, но я не смог найти информацию про них
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCatList(@RequestParam(required = false, defaultValue = "0") int from,
                                                        @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("\nPublicCategoryController: Accepted request for get categories {}, {}", from, size);
        GetCategoriesParams parameters = new GetCategoriesParams(from, size);
        List<CategoryDto> response = categoryService.getCatList(parameters);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
