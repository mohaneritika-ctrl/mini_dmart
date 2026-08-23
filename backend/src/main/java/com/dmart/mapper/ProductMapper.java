package com.dmart.mapper;

import com.dmart.dto.request.ProductRequestDto;
import com.dmart.dto.response.ProductResponseDto;
import com.dmart.entity.Category;
import com.dmart.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto dto, Category category) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .name(dto.getName() != null ? dto.getName().trim() : null)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .price(dto.getPrice())
                .stock(dto.getStock())
                .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .category(category)
                .build();
    }

    public ProductResponseDto toResponseDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(ProductRequestDto dto, Product product, Category category) {
        if (dto == null || product == null) {
            return;
        }

        product.setName(dto.getName() != null ? dto.getName().trim() : product.getName());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : product.getDescription());
        product.setPrice(dto.getPrice() != null ? dto.getPrice() : product.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : product.getStock());
        product.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : product.getImageUrl());
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
        if (category != null) {
            product.setCategory(category);
        }
    }
}