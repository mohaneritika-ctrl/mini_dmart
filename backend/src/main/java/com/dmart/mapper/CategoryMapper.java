package com.dmart.mapper;

import com.dmart.dto.request.CategoryRequestDto;
import com.dmart.dto.response.CategoryResponseDto;
import com.dmart.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();
    }

    public CategoryResponseDto toResponseDto(Category entity) {
        if (entity == null) {
            return null;
        }

        return CategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(CategoryRequestDto dto, Category entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName() != null ? dto.getName().trim() : entity.getName());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : entity.getDescription());
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }
}