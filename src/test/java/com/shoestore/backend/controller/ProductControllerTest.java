package com.shoestore.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoestore.backend.dto.product.CreateProductRequestDto;
import com.shoestore.backend.dto.product.ProductColorResponseDto;
import com.shoestore.backend.dto.product.ProductColorSizeResponseDto;
import com.shoestore.backend.dto.product.ProductDto;
import com.shoestore.backend.dto.product.ProductResponseDto;
import com.shoestore.backend.dto.product.ProductUpdateRequestDto;
import com.shoestore.backend.dto.product.image.CreateProductImageRequestDto;
import com.shoestore.backend.dto.product.image.ProductImageDto;
import com.shoestore.backend.dto.product.image.ProductImageResponseDto;
import com.shoestore.backend.dto.product.image.ProductImageUpdateRequestDto;
import com.shoestore.backend.dto.product.variant.CreateProductVariantRequestDto;
import com.shoestore.backend.dto.product.variant.ProductVariantDto;
import com.shoestore.backend.dto.product.variant.ProductVariantUpdateRequestDto;
import com.shoestore.backend.security.jwt.JwtUtil;
import com.shoestore.backend.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)

public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private JwtUtil jwtUtil;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void createProductReturnsProduct() throws Exception {
        CreateProductRequestDto request = createProductRequest();
        ProductDto response = productDto();
        when(productService.createProduct(any(CreateProductRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Runner"));

        verify(productService).createProduct(any(CreateProductRequestDto.class));
    }

    @Test
    void createProductReturnsBadRequestForInvalidBody() throws Exception {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "",
                "Sneakers",
                "Comfort shoes",
                BigDecimal.TEN,
                null,
                "MEN",
                "SUMMER",
                "Leather"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProductVariantReturnsVariant() throws Exception {
        CreateProductVariantRequestDto request =
                new CreateProductVariantRequestDto("42", "Black", 5, "SKU-1", 10L);
        ProductVariantDto response = new ProductVariantDto(2L, 1L, "42", "Black", 5, "SKU-1");
        when(productService.createProductVariant(eq(1L), any(CreateProductVariantRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/products/{productId}/variants", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.color").value("Black"));

        verify(productService).createProductVariant(
                eq(1L),
                any(CreateProductVariantRequestDto.class)
        );
    }

    @Test
    void createProductImageReturnsImage() throws Exception {
        CreateProductImageRequestDto request =
                new CreateProductImageRequestDto("Black", "main.jpg", List.of("side.jpg"));
        ProductImageDto response = new ProductImageDto(3L, 1L, "Black", "main.jpg",
                List.of("side.jpg"));
        when(productService.createProductImage(eq(1L), any(CreateProductImageRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/products/{productId}/images", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.mainUrl").value("main.jpg"));

        verify(productService).createProductImage(eq(1L), any(CreateProductImageRequestDto.class));
    }

    @Test
    void getAllProductsReturnsProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productResponseDto()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].colors[0]").value("Black"));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductReturnsProduct() throws Exception {
        when(productService.getProduct(1L)).thenReturn(productResponseDto());

        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService).getProduct(1L);
    }

    @Test
    void getProductByColorReturnsColorResponse() throws Exception {
        ProductImageResponseDto image = new ProductImageResponseDto("Black", "main.jpg",
                List.of("side.jpg"));
        ProductColorResponseDto response = new ProductColorResponseDto(
                1L,
                "Sneakers",
                "Runner",
                "Comfort shoes",
                BigDecimal.valueOf(99),
                null,
                "MEN",
                "SUMMER",
                "Leather",
                List.of("42"),
                Instant.parse("2026-01-01T00:00:00Z"),
                image
        );
        when(productService.getProductByColor(1L, "Black")).thenReturn(response);

        mockMvc.perform(get("/api/products/{id}", 1L).param("color", "Black"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizes[0]").value("42"));

        verify(productService).getProductByColor(1L, "Black");
    }

    @Test
    void getProductByColorAndSizeReturnsSizeResponse() throws Exception {
        ProductImageResponseDto image = new ProductImageResponseDto("Black", "main.jpg",
                List.of("side.jpg"));
        ProductColorSizeResponseDto response = new ProductColorSizeResponseDto(
                1L,
                "Sneakers",
                "Runner",
                "Comfort shoes",
                BigDecimal.valueOf(99),
                null,
                "MEN",
                "SUMMER",
                "Leather",
                5,
                Instant.parse("2026-01-01T00:00:00Z"),
                image
        );
        when(productService.getProductByColorAndSize(1L, "Black", "42")).thenReturn(response);

        mockMvc.perform(get("/api/products/{id}", 1L)
                        .param("color", "Black")
                        .param("size", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQty").value(5));

        verify(productService).getProductByColorAndSize(1L, "Black", "42");
    }

    @Test
    void getProductVariantsReturnsVariants() throws Exception {
        when(productService.getProductVariants(1L))
                .thenReturn(List.of(new ProductVariantDto(2L, 1L, "42", "Black", 5, "SKU-1")));

        mockMvc.perform(get("/api/products/{productId}/variants", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));

        verify(productService).getProductVariants(1L);
    }

    @Test
    void getProductImagesReturnsImages() throws Exception {
        when(productService.getProductImages(1L))
                .thenReturn(List.of(new ProductImageDto(3L, 1L, "Black", "main.jpg",
                        List.of("side.jpg"))));

        mockMvc.perform(get("/api/products/{productId}/images", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mainUrl").value("main.jpg"));

        verify(productService).getProductImages(1L);
    }

    @Test
    void updateProductReturnsProduct() throws Exception {
        ProductUpdateRequestDto request = new ProductUpdateRequestDto(
                "Sneakers",
                "Runner 2",
                "Updated",
                BigDecimal.valueOf(109),
                null,
                "MEN",
                "SUMMER",
                "Leather"
        );
        when(productService.updateProduct(eq(1L), any(ProductUpdateRequestDto.class)))
                .thenReturn(productDto());

        mockMvc.perform(patch("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(productService).updateProduct(eq(1L), any(ProductUpdateRequestDto.class));
    }

    @Test
    void updateProductVariantReturnsVariant() throws Exception {
        ProductVariantUpdateRequestDto request =
                new ProductVariantUpdateRequestDto("43", "Black", 6, "SKU-2");
        ProductVariantDto response = new ProductVariantDto(2L, 1L, "43", "Black", 6, "SKU-2");
        when(productService.updateProductVariant(eq(2L), any(ProductVariantUpdateRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/products/variants/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("SKU-2"));

        verify(productService).updateProductVariant(
                eq(2L),
                any(ProductVariantUpdateRequestDto.class)
        );
    }

    @Test
    void updateProductImageReturnsImage() throws Exception {
        ProductImageUpdateRequestDto request =
                new ProductImageUpdateRequestDto("Black", "main-new.jpg", List.of("side.jpg"));
        ProductImageDto response = new ProductImageDto(3L, 1L, "Black", "main-new.jpg",
                List.of("side.jpg"));
        when(productService.updateProductImage(eq(3L), eq("Black"),
                any(ProductImageUpdateRequestDto.class))).thenReturn(response);

        mockMvc.perform(patch("/api/products/images/{id}/{color}", 3L, "Black")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mainUrl").value("main-new.jpg"));

        verify(productService).updateProductImage(eq(3L), eq("Black"),
                any(ProductImageUpdateRequestDto.class));
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProductVariantReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProductVariant(2L);

        mockMvc.perform(delete("/api/products/variants/{id}", 2L))
                .andExpect(status().isNoContent());

        verify(productService).deleteProductVariant(2L);
    }

    @Test
    void deleteProductImageReturnsNoContent() throws Exception {
        doNothing().when(productService).deleteProductImage(3L, "Black");

        mockMvc.perform(delete("/api/products/images/{id}/{color}", 3L, "Black"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProductImage(3L, "Black");
    }

    private CreateProductRequestDto createProductRequest() {
        return new CreateProductRequestDto(
                "Runner",
                "Sneakers",
                "Comfort shoes",
                BigDecimal.valueOf(99),
                null,
                "MEN",
                "SUMMER",
                "Leather"
        );
    }

    private ProductDto productDto() {
        return new ProductDto(
                1L,
                "Sneakers",
                "Runner",
                "Comfort shoes",
                BigDecimal.valueOf(99),
                null,
                "MEN",
                "SUMMER",
                "Leather",
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }

    private ProductResponseDto productResponseDto() {
        ProductImageResponseDto image = new ProductImageResponseDto("Black", "main.jpg",
                List.of("side.jpg"));
        return new ProductResponseDto(
                1L,
                "Sneakers",
                "Runner",
                "Comfort shoes",
                BigDecimal.valueOf(99),
                null,
                "MEN",
                "SUMMER",
                "Leather",
                List.of("Black"),
                Instant.parse("2026-01-01T00:00:00Z"),
                List.of(image)
        );
    }
}
