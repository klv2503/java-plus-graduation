package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.dto.category.CategoryDto;

@FeignClient(name = "main-service", path = "/categories")
public interface EventCategoryFeign {

    @GetMapping("/{catId}")
    ResponseEntity<CategoryDto> getInfoById(@PathVariable Long catId);

}
