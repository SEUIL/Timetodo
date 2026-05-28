package org.timetodo.controller;

import jakarta.servlet.http.HttpServletRequest;
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

    // 카테고리 추가
    @PostMapping("/add")
    public ResponseEntity<CategoryResponseDto> addCategory(@RequestBody CategoryRequestDto categoryDto, HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto category = categoryService.createCategory(categoryDto, userId);
        return ResponseEntity.ok(category);
    }

    // 특정 사용자의 모든 카테고리 조회
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories(HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        List<CategoryResponseDto> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    // 특정 카테고리 업데이트
    @PutMapping("/update/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long id,
                                                              @RequestBody CategoryRequestDto categoryDto,
                                                              HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto updatedCategory = categoryService.updateCategory(id, categoryDto, userId);
        return ResponseEntity.ok(updatedCategory);
    }

    // 특정 카테고리 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        categoryService.deleteCategoryById(userId, id);
        return ResponseEntity.ok("카테고리 삭제 성공");
    }

    // 카테고리 이름으로 검색
    @GetMapping("/search")
    public ResponseEntity<CategoryResponseDto> searchCategoryByName(@RequestParam String categoryName,
                                                                    HttpServletRequest request) {
        Long userId = currentUserService.requireCurrentUserId(request);
        CategoryResponseDto category = categoryService.getCategoryByName(userId, categoryName);
        return ResponseEntity.ok(category);
    }
}
