package com.dmart.service;

import com.dmart.dto.request.ProductRequestDto;
import com.dmart.dto.request.UpdateStockRequestDto;
import com.dmart.dto.response.PageResponseDto;
import com.dmart.dto.response.ProductResponseDto;
import com.dmart.entity.Category;
import com.dmart.entity.Product;
import com.dmart.exception.ResourceNotFoundException;
import com.dmart.mapper.ProductMapper;
import com.dmart.repository.CategoryRepository;
import com.dmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "price", "stock", "createdAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        Product product = productMapper.toEntity(dto, category);
        Product saved = productRepository.save(product);
        return productMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!Boolean.TRUE.equals(product.getActive()) && !isStaffOrAdmin()) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }

        return productMapper.toResponseDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getAllProducts(int page, int size, String sortBy, String direction, Boolean includeInactive) {
        return getFilteredProducts(page, size, sortBy, direction, null, null, includeInactive);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> getFilteredProducts(int page, int size, String sortBy, String direction, String keyword, Long categoryId, Boolean includeInactive) {
        Pageable pageable = createPageable(page, size, sortBy, direction);
        boolean shouldIncludeInactive = isStaffOrAdmin() && Boolean.TRUE.equals(includeInactive);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId != null;

        Page<Product> productPage;
        if (hasKeyword && hasCategory) {
            if (shouldIncludeInactive) {
                productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryId(keyword.trim(), categoryId, pageable);
            } else {
                productPage = productRepository.findByNameContainingIgnoreCaseAndCategoryIdAndActiveTrue(keyword.trim(), categoryId, pageable);
            }
        } else if (hasKeyword) {
            if (shouldIncludeInactive) {
                productPage = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
            } else {
                productPage = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword.trim(), pageable);
            }
        } else if (hasCategory) {
            if (shouldIncludeInactive) {
                productPage = productRepository.findByCategoryId(categoryId, pageable);
            } else {
                productPage = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
            }
        } else {
            if (shouldIncludeInactive) {
                productPage = productRepository.findAll(pageable);
            } else {
                productPage = productRepository.findByActiveTrue(pageable);
            }
        }

        return toPageResponse(productPage);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        productMapper.updateEntityFromDto(dto, product, category);
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<ProductResponseDto> searchProducts(String name, int page, int size, String sortBy, String direction, Boolean includeInactive) {
        if (name == null || name.trim().isEmpty()) {
            return getAllProducts(page, size, sortBy, direction, includeInactive);
        }

        Pageable pageable = createPageable(page, size, sortBy, direction);
        boolean shouldIncludeInactive = isStaffOrAdmin() && Boolean.TRUE.equals(includeInactive);

        Page<Product> productPage;
        if (shouldIncludeInactive) {
            productPage = productRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
        } else {
            productPage = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name.trim(), pageable);
        }

        return toPageResponse(productPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByCategory(Long categoryId, Boolean includeInactive) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with ID: " + categoryId);
        }

        boolean shouldIncludeInactive = isStaffOrAdmin() && Boolean.TRUE.equals(includeInactive);
        List<Product> products;
        if (shouldIncludeInactive) {
            products = productRepository.findByCategoryId(categoryId);
        } else {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId);
        }

        return products.stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto updateStock(Long id, UpdateStockRequestDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setStock(dto.getStock());
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public ProductResponseDto activateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setActive(true);
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public ProductResponseDto deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setActive(false);
        Product updated = productRepository.save(product);
        return productMapper.toResponseDto(updated);
    }

    private Pageable createPageable(int page, int size, String sortBy, String direction) {
        int safePage = Math.max(0, page);
        int safeSize = (size <= 0 || size > 100) ? 10 : size;

        String safeSortBy = (sortBy != null && ALLOWED_SORT_FIELDS.contains(sortBy)) ? sortBy : "id";
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(safePage, safeSize, Sort.by(sortDirection, safeSortBy));
    }

    private PageResponseDto<ProductResponseDto> toPageResponse(Page<Product> page) {
        List<ProductResponseDto> content = page.getContent().stream()
                .map(productMapper::toResponseDto)
                .toList();

        return PageResponseDto.<ProductResponseDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
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