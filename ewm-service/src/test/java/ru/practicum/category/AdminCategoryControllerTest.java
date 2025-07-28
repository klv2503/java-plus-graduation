package ru.practicum.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.MainService;
import ru.practicum.category.controller.AdminCategoryController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@ContextConfiguration(classes = MainService.class)
@RequiredArgsConstructor
public class AdminCategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CategoryDto goodCat1 = CategoryDto.builder()
            .id(1L)
            .name("Football")
            .build();

    @Test
    @SneakyThrows
    public void addCategory_whenValidCategoryDto_thenAdd() {

        NewCategoryDto nCategory = new NewCategoryDto("Football");
        when(categoryService.addCategory(nCategory)).thenReturn(goodCat1);
        RequestBuilder request = MockMvcRequestBuilders
                .post("/admin/categories")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nCategory))
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult mvcResult = mockMvc.perform(request).andExpect(status().isCreated()).andReturn();

        assertNotNull(mvcResult.getResponse());
        verify(categoryService, times(1)).addCategory(nCategory);

    }

    @Test
    @SneakyThrows
    public void addCategory_whenBlankCategoryName_thenBadRequest() {

        NewCategoryDto nCategory = new NewCategoryDto("    ");
        when(categoryService.addCategory(nCategory))
                .thenThrow(new RuntimeException("Got incorrect requestBody"));
        RequestBuilder request = MockMvcRequestBuilders
                .post("/admin/categories")
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nCategory))
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());

        verify(categoryService, times(0)).addCategory(nCategory);

    }

    @Test
    @SneakyThrows
    public void deleteCategory_whenValidId_thenDelete() {

        Long id = 1L;
        doNothing().when(categoryService).deleteCategory(id);
        RequestBuilder request = MockMvcRequestBuilders
                .delete("/admin/categories/{catId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(id);

    }

    @Test
    @SneakyThrows
    public void deleteCategory_whenInvalidId_thenThrow() {

        Long id = 0L;
        doThrow(new RuntimeException("Incorrect id")).when(categoryService).deleteCategory(id);
        RequestBuilder request = MockMvcRequestBuilders
                .delete("/admin/categories/{catId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());

        verify(categoryService, times(0)).deleteCategory(id);

    }

    @Test
    @SneakyThrows
    public void updateCategory_whenValidDto_thenUpdate() {

        Long id = 1L;
        when(categoryService.updateCategory(goodCat1)).thenReturn(goodCat1);
        RequestBuilder request = MockMvcRequestBuilders
                .patch("/admin/categories/{catId}", id)
                .content(objectMapper.writeValueAsString(goodCat1))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isOk()).andReturn();

        verify(categoryService, times(1)).updateCategory(goodCat1);

    }

    @Test
    @SneakyThrows
    public void updateCategory_whenInvalidDto_thenThrow() {

        CategoryDto nCategory = new CategoryDto(1L, "       ");
        Long id = 0L;
        when(categoryService.updateCategory(nCategory))
                .thenThrow(new RuntimeException("Got incorrect requestBody"));
        RequestBuilder request = MockMvcRequestBuilders
                .patch("/admin/categories/{catId}", id)
                .content(objectMapper.writeValueAsString(nCategory))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request).andExpect(status().isBadRequest());

        verify(categoryService, times(0)).updateCategory(nCategory);

    }

}
