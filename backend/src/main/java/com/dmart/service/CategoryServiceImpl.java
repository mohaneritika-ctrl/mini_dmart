package com.dmart.service;

import com.dmart.dto.request.CategoryRequestDto;
import com.dmart.dto.response.CategoryResponseDto;
import com.dmart.entity.Category;
import com.dmart.exception.ConflictException;
import com.dmart.exception.DuplicateResourceException;
import com.dmart.exception.ResourceNotFoundException;
import com.dmart.mapper.CategoryMapper;
import com.dmart.repository.CategoryRepository;
import com.dmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        String trimmedName = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Category already exists with name: " + trimmedName);
        }

        Category category = categoryMapper.toEntity(dto);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (!Boolean.TRUE.equals(category.getActive()) && !isStaffOrAdmin()) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }

        return categoryMapper.toResponseDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        List<Category> categories;
        if (isStaffOrAdmin()) {
            categories = categoryRepository.findAll();
        } else {
            categories = categoryRepository.findByActiveTrue();
        }

        return categories.stream()
                .map(categoryMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        String trimmedName = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Another category already exists with name: " + trimmedName);
        }

        categoryMapper.updateEntityFromDto(dto, category);
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Cannot delete category '" + category.getName()
                    + "' (ID: " + id + ") because it contains associated products. Consider deactivating the category instead.");
        }

        categoryRepository.delete(category);
    }

    private boolean isStaffOrAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STAFF") || a.getAuthority().equals("ROLE_ADMIN"));
    }
}