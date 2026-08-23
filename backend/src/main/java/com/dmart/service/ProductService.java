package com.dmart.service;

import com.dmart.dto.request.ProductRequestDto;
import com.dmart.dto.request.UpdateStockRequestDto;
import com.dmart.dto.response.PageResponseDto;
import com.dmart.dto.response.ProductResponseDto;

import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto);

    ProductResponseDto getProductById(Long id);

    PageResponseDto<ProductResponseDto> getAllProducts(int page, int size, String sortBy, String direction, Boolean includeInactive);

    PageResponseDto<ProductResponseDto> getFilteredProducts(int page, int size, String sortBy, String direction, String keyword, Long categoryId, Boolean includeInactive);

    ProductResponseDto updateProduct(Long id, ProductRequestDto dto);

    void deleteProduct(Long id);

    PageResponseDto<ProductResponseDto> searchProducts(String name, int page, int size, String sortBy, String direction, Boolean includeInactive);

    List<ProductResponseDto> getProductsByCategory(Long categoryId, Boolean includeInactive);

    ProductResponseDto updateStock(Long id, UpdateStockRequestDto dto);

    ProductResponseDto activateProduct(Long id);

    ProductResponseDto deactivateProduct(Long id);
}