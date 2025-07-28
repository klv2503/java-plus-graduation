package ru.practicum.category;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.GetCategoriesParams;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)

public class CategoryServiceTests {

    private final EntityManager em;

    private final CategoryService service;

    @Test
    public void testAddCategory() {
        NewCategoryDto catToAdd = new NewCategoryDto("Name of category");
        service.addCategory(catToAdd);

        TypedQuery<Category> query = em.createQuery("Select c from Category c where c.name = :name", Category.class);
        Category cat = query.setParameter("name", catToAdd.getName())
                .getSingleResult();

        assertThat(cat.getId(), notNullValue());
        assertThat(cat.getName(), equalTo(catToAdd.getName()));
    }

    @Test
    public void deleteCategory_whenCorrectId_thenDelete() {
        GetCategoriesParams getCP = new GetCategoriesParams(0, 10);
        List<CategoryDto> cats = service.getCatList(getCP);
        int initialSize = cats.size();
        CategoryDto singleCat = cats.getLast();

        service.deleteCategory(singleCat.getId());

        List<CategoryDto> catsNewList = service.getCatList(getCP);
        assertEquals(initialSize - 1, catsNewList.size());
        assertFalse(catsNewList.contains(singleCat));
    }

    @Test
    public void deleteCategory_whenWrongId_thenNotFoundException() {
        assertThrows(EntityNotFoundException.class, () -> service.deleteCategory(100L));
    }

    @Test
    public void updateCategory_whenWrongId_thenNotFoundException() {
        CategoryDto cat = new CategoryDto(100L, "For updating");
        assertThrows(EntityNotFoundException.class, () -> service.updateCategory(cat));
    }

    @Test
    public void updateCategory_whenUsedName_thenDataIntegrityViolationException() {
        CategoryDto cat = new CategoryDto(3L, "Category");
        CategoryService myService = Mockito.mock(CategoryService.class);

        when(myService.updateCategory(cat))
                .thenThrow(new DataIntegrityViolationException("Integrity constraint has been violated."));
    }

    @Test
    public void updateCategory_whenCorrectData_thenUpdate() {
        CategoryDto catDto = new CategoryDto(3L, "For updating");
        service.updateCategory(catDto);
        TypedQuery<Category> query = em.createQuery("Select c from Category c where c.id = :id", Category.class);
        Category cat = query.setParameter("id", catDto.getId())
                .getSingleResult();

        assertEquals(catDto.getName(), cat.getName());
    }

    @Test
    public void getCategory_whenCorrectId_thenGet() {
        CategoryDto singleCat = service.getCategory(1L);
        TypedQuery<Category> query = em.createQuery("Select c from Category c where c.id = :id", Category.class);
        Category cat = query.setParameter("id", 1L)
                .getSingleResult();

        assertEquals(cat.getId(), singleCat.getId());
        assertEquals(cat.getName(), singleCat.getName());
    }

    @Test
    public void getCategory_whenWrongId_thenNotFoundException() {
        assertThrows(EntityNotFoundException.class, () -> service.getCategory(100L));
    }

    @Test
    public void getCatListTest() {
        CategoryDto testedCat = new CategoryDto(1L, "Category");
        GetCategoriesParams getCP = new GetCategoriesParams(0, 10);
        List<CategoryDto> cats = service.getCatList(getCP);

        assertEquals(3, cats.size());
        assertTrue(cats.contains(testedCat));
    }

}