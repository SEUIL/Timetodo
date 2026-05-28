package org.timetodo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.timetodo.auth.CurrentUserService;
import org.timetodo.dto.CategoryRequestDto;
import org.timetodo.dto.CategoryResponseDto;
import org.timetodo.service.CategoryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<CategoryResponseDto> addCategory(@Valid @RequestBody CategoryRequestDto categoryDto, HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto category = categoryService.createCategory(categoryDto, userId);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        List<CategoryResponseDto> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody CategoryRequestDto categoryDto,
                                                              HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, categoryDto, userId);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        categoryService.deleteCategoryById(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<CategoryResponseDto> searchCategoryByName(@RequestParam String categoryName,
                                                                    HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto category = categoryService.getCategoryByName(userId, categoryName);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/add")
    public ResponseEntity<CategoryResponseDto> addCategoryLegacy(@Valid @RequestBody CategoryRequestDto categoryDto, HttpServletRequest request) {
        return addCategory(categoryDto, request);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategoriesLegacy(HttpServletRequest request) {
        return getAllCategories(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategoryLegacy(@PathVariable Long id,
                                                                    @Valid @RequestBody CategoryRequestDto categoryDto,
                                                                    HttpServletRequest request) {
        return updateCategory(id, categoryDto, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCategoryLegacy(@PathVariable Long id, HttpServletRequest request) {
        return deleteCategory(id, request);
    }
}
