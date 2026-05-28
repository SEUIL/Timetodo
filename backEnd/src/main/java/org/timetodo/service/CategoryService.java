package org.timetodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.timetodo.dto.CategoryRequestDto;
import org.timetodo.dto.CategoryResponseDto;
import org.timetodo.entity.CategoryEntity;
import org.timetodo.entity.UserEntity;
import org.timetodo.repository.CategoryRepository;
import org.timetodo.repository.UserRepository;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    // 카테고리 생성
    public CategoryResponseDto createCategory(CategoryRequestDto categoryDto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        CategoryEntity category = new CategoryEntity();
        category.setCategoryName(categoryDto.getCategoryName());
        category.setColor(categoryDto.getColor());
        category.setUser(user);
        CategoryEntity savedCategory = categoryRepository.save(category);

        return convertToResponseDto(savedCategory);
    }

    private CategoryResponseDto convertToResponseDto(CategoryEntity category) {
        return new CategoryResponseDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getColor()
        );
    }
    // 특정 사용자에 대한 카테고리 목록 조회
    public List<CategoryResponseDto> getCategoriesByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        List<CategoryEntity> categories = categoryRepository.findByUser(user);
        return categories.stream()
                .map(category -> new CategoryResponseDto(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        category.getColor()
                ))
                .toList();
    }

    // 카테고리 업데이트
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto categoryDto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        validateCategoryOwner(category, userId);

        category.setCategoryName(categoryDto.getCategoryName());
        category.setColor(categoryDto.getColor());
        categoryRepository.save(category);
        return convertToResponseDto(category);
    }

    public void deleteCategoryById(Long userId, Long categoryId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        // 카테고리 조회
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."));
        validateCategoryOwner(category, user.getUserId());

        // 카테고리 삭제
        categoryRepository.delete(category);
    }

    public CategoryResponseDto getCategoryByName(Long userId, String categoryName) {
        // 사용자 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        // 사용자와 카테고리 이름으로 카테고리 조회
        CategoryEntity category = categoryRepository.findByUserAndCategoryName(user, categoryName);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 이름의 카테고리를 찾을 수 없습니다.");
        }

        // CategoryResponseDto로 변환하여 반환
        return convertToResponseDto(category);
    }

    private void validateCategoryOwner(CategoryEntity category, Long userId) {
        if (category.getUser() == null || !userId.equals(category.getUser().getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 카테고리에 접근할 수 없습니다.");
        }
    }

}
