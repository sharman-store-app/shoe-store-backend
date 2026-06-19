package com.shoestore.backend.controller;

import com.shoestore.backend.dto.product.CreateProductRequestDto;
import com.shoestore.backend.dto.product.ProductColorResponseDto;
import com.shoestore.backend.dto.product.ProductColorSizeResponseDto;
import com.shoestore.backend.dto.product.ProductDto;
import com.shoestore.backend.dto.product.ProductResponseDto;
import com.shoestore.backend.dto.product.ProductUpdateRequestDto;
import com.shoestore.backend.dto.product.image.CreateProductImageRequestDto;
import com.shoestore.backend.dto.product.image.ProductImageDto;
import com.shoestore.backend.dto.product.image.ProductImageUpdateRequestDto;
import com.shoestore.backend.dto.product.variant.CreateProductVariantRequestDto;
import com.shoestore.backend.dto.product.variant.ProductVariantDto;
import com.shoestore.backend.dto.product.variant.ProductVariantUpdateRequestDto;
import com.shoestore.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. Products", description = "Product related endpoints")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create product",
            description = "Creates a new product. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductDto createProduct(@RequestBody @Valid CreateProductRequestDto request) {
        return productService.createProduct(request);
    }

    @PostMapping("/{productId}/variants")
    @Operation(summary = "Create product variant",
            description = "Creates a new product variant. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductVariantDto createProductVariant(@PathVariable Long productId,
                                           @RequestBody @Valid
                                           CreateProductVariantRequestDto request) {
        return productService.createProductVariant(productId, request);
    }

    @PostMapping("/{productId}/images")
    @Operation(summary = "Create product images",
            description = "Creates a new product image. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductImageDto createProductImage(@PathVariable Long productId,
                                       @RequestBody @Valid
                                       CreateProductImageRequestDto request) {
        return productService.createProductImage(productId, request);
    }

    @GetMapping
    @Operation(summary = "Get products", description = "Get information about all products")
    List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product", description = "Get information about product")
    ProductResponseDto getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping(value = "/{id}", params = "color")
    @Operation(summary = "Get product by id and color",
            description = "Get product information for the selected color.")
    ProductColorResponseDto getProductByColor(@PathVariable Long id, @RequestParam String color) {
        return productService.getProductByColor(id, color);
    }

    @GetMapping(value = "/{id}", params = {"color", "size"})
    @Operation(summary = "Get product by id, color and size",
            description = "Get product information for the selected color and size.")
    ProductColorSizeResponseDto getProductByColor(@PathVariable Long id,
                                                  @RequestParam String color,
                                                  @RequestParam String size) {
        return productService.getProductByColorAndSize(id, color, size);
    }

    @PatchMapping(value = "/{id}")
    @Operation(summary = "Update product",
            description = "Updates a product. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductDto updateProduct(@PathVariable Long id,
                             @RequestBody ProductUpdateRequestDto request) {
        return productService.updateProduct(id, request);
    }

    @PatchMapping(value = "/variants/{id}")
    @Operation(summary = "Update product variant",
            description = "Updates a product variant. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductVariantDto updateProductVariant(@PathVariable Long id,
                                           @RequestBody ProductVariantUpdateRequestDto request) {
        return productService.updateProductVariant(id, request);
    }

    @PatchMapping(value = "/images/{id}/{color}")
    @Operation(summary = "Update product image",
            description = "Updates a product image. Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    ProductImageDto updateProductImage(@PathVariable Long id, @PathVariable String color,
                                       @RequestBody ProductImageUpdateRequestDto request) {
        return productService.updateProductImage(id, color, request);
    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete product", description = "Delete product. "
            + "Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @DeleteMapping(value = "/variants/{id}")
    @Operation(summary = "Delete product variant", description = "Delete product variant. "
            + "Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteProductVariant(@PathVariable Long id) {
        productService.deleteProductVariant(id);
    }

    @DeleteMapping(value = "/images/{id}")
    @Operation(summary = "Delete product image", description = "Delete product image. "
            + "Accessible only to ADMIN users.")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteProductImage(@PathVariable Long id) {
        productService.deleteProductImage(id);
    }
}
