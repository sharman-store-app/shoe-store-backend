package com.shoestore.backend.service;

import com.shoestore.backend.dto.product.ProductUpdateRequestDto;
import com.shoestore.backend.dto.product.image.CreateProductImageRequestDto;
import com.shoestore.backend.dto.product.CreateProductRequestDto;
import com.shoestore.backend.dto.product.variant.CreateProductVariantRequestDto;
import com.shoestore.backend.dto.product.ProductColorResponseDto;
import com.shoestore.backend.dto.product.ProductColorSizeResponseDto;
import com.shoestore.backend.dto.product.ProductDto;
import com.shoestore.backend.dto.product.image.ProductImageDto;
import com.shoestore.backend.dto.product.image.ProductImageUpdateRequestDto;
import com.shoestore.backend.dto.product.ProductResponseDto;
import com.shoestore.backend.dto.product.variant.ProductVariantDto;
import com.shoestore.backend.dto.product.variant.ProductVariantUpdateRequestDto;
import java.util.List;

public interface ProductService {

    ProductDto createProduct(CreateProductRequestDto request);

    ProductVariantDto createProductVariant(Long productId, CreateProductVariantRequestDto request);

    ProductImageDto createProductImage(Long productId, CreateProductImageRequestDto request);

    List<ProductResponseDto> getAllProducts();

    ProductResponseDto getProduct(Long id);

    ProductColorResponseDto getProductByColor(Long id, String color);

    ProductColorSizeResponseDto getProductByColorAndSize(Long id, String color, String size);

    ProductDto updateProduct(Long id, ProductUpdateRequestDto request);

    ProductVariantDto updateProductVariant(Long id, ProductVariantUpdateRequestDto request);

    ProductImageDto updateProductImage(Long id, ProductImageUpdateRequestDto request);

    void deleteProduct(Long id);

    void deleteProductVariant(Long id);

    void deleteProductImage(Long id);
}
