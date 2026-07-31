package com.shoestore.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.shoestore.backend.dto.product.CreateProductRequestDto;
import com.shoestore.backend.dto.product.ProductColorSizeResponseDto;
import com.shoestore.backend.dto.product.ProductDto;
import com.shoestore.backend.dto.product.ProductResponseDto;
import com.shoestore.backend.dto.product.ProductUpdateRequestDto;
import com.shoestore.backend.dto.product.image.CreateProductImageRequestDto;
import com.shoestore.backend.dto.product.variant.CreateProductVariantRequestDto;
import com.shoestore.backend.dto.product.variant.ProductVariantDto;
import com.shoestore.backend.dto.product.variant.ProductVariantUpdateRequestDto;
import com.shoestore.backend.exceptation.EntityAlreadyExistsException;
import com.shoestore.backend.mapper.ProductMapper;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void createProductSavesMappedProduct() {
        CreateProductRequestDto request = productRequest("Runner");
        Product product = product(1L, "Runner");
        ProductDto dto = productDto(1L, "Runner");
        when(productRepository.findByName("Runner")).thenReturn(Optional.empty());
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDto response = service.createProduct(request);

        assertEquals(dto, response);
        verify(productRepository).save(product);
    }

    @Test
    void createProductThrowsWhenNameExists() {
        CreateProductRequestDto request = productRequest("Runner");
        when(productRepository.findByName("Runner")).thenReturn(Optional.of(new Product()));

        assertThrows(EntityAlreadyExistsException.class, () -> service.createProduct(request));
        verifyNoInteractions(productMapper);
    }

    @Test
    void createProductVariantAttachesVariantToProduct() {
        Product product = product(1L, "Runner");
        CreateProductVariantRequestDto request =
                new CreateProductVariantRequestDto("42", "Black", 3, "sku", null);
        ProductVariant variant = new ProductVariant().setId(5L);
        ProductVariantDto dto = new ProductVariantDto(5L, 1L, "42", "Black", 3, "sku");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.existsByProductIdAndColorAndSize(1L, "Black", "42"))
                .thenReturn(false);
        when(productMapper.toProductVariant(request)).thenReturn(variant);
        when(productVariantRepository.save(variant)).thenReturn(variant);
        when(productMapper.toProductVariantDto(variant)).thenReturn(dto);

        ProductVariantDto response = service.createProductVariant(1L, request);

        assertEquals(dto, response);
        assertEquals(product, variant.getProduct());
    }

    @Test
    void createProductImageThrowsWhenColorAlreadyExists() {
        Product product = product(1L, "Runner");
        CreateProductImageRequestDto request =
                new CreateProductImageRequestDto("Black", "main", List.of("extra"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productImageRepository.existsByProductIdAndColor(1L, "Black")).thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class, () ->
                service.createProductImage(1L, request));
    }

    @Test
    void getAllProductsReturnsMappedProductsWithImagesAndVariants() {
        Product product = product(1L, "Runner");
        ProductImage image = image(2L, product, "Black");
        ProductVariant variant = variant(3L, product, "Black", "42", 2);
        ProductResponseDto dto = productResponseDto(1L, "Runner");
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productImageRepository.findByProductIdIn(List.of(1L))).thenReturn(List.of(image));
        when(productVariantRepository.findByProductIdIn(List.of(1L)))
                .thenReturn(List.of(variant));
        when(productMapper.toProductDto(product, List.of(image), List.of(variant)))
                .thenReturn(dto);

        List<ProductResponseDto> response = service.getAllProducts();

        assertEquals(List.of(dto), response);
    }

    @Test
    void getProductByColorAndSizeThrowsWhenSizeIsMissing() {
        Product product = product(1L, "Runner");
        ProductVariant variant = variant(3L, product, "Black", "42", 2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(List.of(variant));

        assertThrows(EntityNotFoundException.class, () ->
                service.getProductByColorAndSize(1L, "Black", "43"));
    }

    @Test
    void getProductByColorAndSizeMapsMatchingVariant() {
        Product product = product(1L, "Runner");
        ProductVariant variant = variant(3L, product, "Black", "42", 2);
        ProductImage image = image(2L, product, "Black");
        ProductColorSizeResponseDto dto = new ProductColorSizeResponseDto(
                1L,
                "Shoes",
                "Runner",
                "Desc",
                BigDecimal.TEN,
                null,
                "Unisex",
                "Summer",
                "Mesh",
                2,
                null,
                null
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(List.of(variant));
        when(productImageRepository.findByProductIdAndColor(1L, "Black"))
                .thenReturn(Optional.of(image));
        when(productMapper.toProductColorSizeDto(product, variant, image)).thenReturn(dto);

        ProductColorSizeResponseDto response =
                service.getProductByColorAndSize(1L, "Black", "42");

        assertEquals(dto, response);
    }

    @Test
    void updateProductOnlyAppliesNonNullFields() {
        Product product = product(1L, "Runner");
        ProductUpdateRequestDto request = new ProductUpdateRequestDto(
                null,
                "New Runner",
                null,
                new BigDecimal("120.00"),
                null,
                null,
                null,
                null
        );
        ProductDto dto = productDto(1L, "New Runner");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(dto);

        ProductDto response = service.updateProduct(1L, request);

        assertEquals(dto, response);
        assertEquals("New Runner", product.getName());
        assertEquals(new BigDecimal("120.00"), product.getPrice());
    }

    @Test
    void updateProductVariantThrowsWhenSkuBelongsToAnotherVariant() {
        Product product = product(1L, "Runner");
        ProductVariant variant = variant(3L, product, "Black", "42", 2);
        ProductVariant otherVariant = variant(4L, product, "Black", "43", 2);
        ProductVariantUpdateRequestDto request =
                new ProductVariantUpdateRequestDto(null, null, null, "sku2");
        when(productVariantRepository.findById(3L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.findBySku("sku2")).thenReturn(Optional.of(otherVariant));

        assertThrows(InputMismatchException.class, () ->
                service.updateProductVariant(3L, request));
    }

    @Test
    void deleteProductDeletesChildrenBeforeProduct() {
        Product product = product(1L, "Runner");
        ProductVariant variant = variant(3L, product, "Black", "42", 2);
        ProductImage image = image(2L, product, "Black");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(variant));
        when(productImageRepository.findByProductId(1L)).thenReturn(List.of(image));

        service.deleteProduct(1L);

        verify(productVariantRepository).delete(variant);
        verify(productImageRepository).delete(image);
        verify(productRepository).delete(product);
    }

    private CreateProductRequestDto productRequest(String name) {
        return new CreateProductRequestDto(
                name,
                "Shoes",
                "Desc",
                BigDecimal.TEN,
                null,
                "Unisex",
                "Summer",
                "Mesh"
        );
    }

    private Product product(Long id, String name) {
        return new Product()
                .setId(id)
                .setName(name)
                .setCategory("Shoes")
                .setDescription("Desc")
                .setPrice(BigDecimal.TEN)
                .setGender("Unisex")
                .setSeason("Summer")
                .setMaterial("Mesh");
    }

    private ProductVariant variant(
            Long id,
            Product product,
            String color,
            String size,
            int stockQty) {
        return new ProductVariant()
                .setId(id)
                .setProduct(product)
                .setColor(color)
                .setSize(size)
                .setStockQty(stockQty)
                .setSku("sku" + id);
    }

    private ProductImage image(Long id, Product product, String color) {
        return new ProductImage()
                .setId(id)
                .setProduct(product)
                .setColor(color)
                .setMainUrl("main")
                .setUrls(List.of("extra"));
    }

    private ProductDto productDto(Long id, String name) {
        return new ProductDto(
                id,
                "Shoes",
                name,
                "Desc",
                BigDecimal.TEN,
                null,
                "Unisex",
                "Summer",
                "Mesh",
                null
        );
    }

    private ProductResponseDto productResponseDto(Long id, String name) {
        return new ProductResponseDto(
                id,
                "Shoes",
                name,
                "Desc",
                BigDecimal.TEN,
                null,
                "Unisex",
                "Summer",
                "Mesh",
                List.of("Black"),
                null,
                List.of()
        );
    }
}
