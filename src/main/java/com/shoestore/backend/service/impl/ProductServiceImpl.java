package com.shoestore.backend.service.impl;

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
import com.shoestore.backend.exceptation.EntityAlreadyExistsException;
import com.shoestore.backend.mapper.ProductMapper;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import com.shoestore.backend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequestDto request) {
        if (productRepository.findByName(request.name()).isPresent()) {
            throw new EntityAlreadyExistsException("Product " + request.name()
                    + " already exists in database");
        }
        Product product = productMapper.toEntity(request);
        product.setCreatedAt(Instant.now());
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductVariantDto createProductVariant(Long productId,
                                                  CreateProductVariantRequestDto request) {
        Product product = findProduct(productId);
        if (productVariantRepository.existsByProductIdAndColorAndSize(productId, request.color(),
                request.size())) {
            throw new EntityAlreadyExistsException("Variant " + "for product with id " + productId
                    + " in color " + request.color() + " and size " + request.size()
                    + " already exists in database");
        }
        ProductVariant productVariant = productMapper.toProductVariant(request);
        productVariant.setProduct(product);
        ProductVariant savedProductVariant = productVariantRepository.save(productVariant);
        return productMapper.toProductVariantDto(savedProductVariant);
    }

    @Override
    @Transactional
    public ProductImageDto createProductImage(Long productId,
                                              CreateProductImageRequestDto request) {
        Product product = findProduct(productId);
        if (productImageRepository.existsByColorAndMainUrl(request.color(),
                request.mainUrl())) {
            throw new EntityAlreadyExistsException("Image with given color and main url "
                    + "already exists in database");
        }
        ProductImage productImage = productMapper.toProductImage(request);
        productImage.setProduct(product);
        ProductImage savedProductImage = productImageRepository.save(productImage);
        return productMapper.toProductImageDto(savedProductImage);
    }

    @Override
    public List<ProductResponseDto> getAllProducts() {

        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productsIds = products.stream()
                .map(Product::getId)
                .toList();
        List<ProductImage> productImages = productImageRepository
                .findByProductIdIn(productsIds);
        List<ProductVariant> productVariants = productVariantRepository
                .findByProductIdIn(productsIds);

        return products.stream()
                .map(product -> productMapper.toProductDto(product, productImages, productVariants))
                .toList();
    }

    @Override
    public ProductResponseDto getProduct(Long id) {
        Product product = findProduct(id);
        List<ProductImage> productImages = productImageRepository.findByProductId(id);
        List<ProductVariant> productVariants = productVariantRepository.findByProductId(id);

        return productMapper.toProductDto(product, productImages, productVariants);
    }

    @Override
    public ProductColorResponseDto getProductByColor(Long id, String color) {
        Product product = findProduct(id);
        List<ProductVariant> productVariants = findProductByIdAndColor(id, color);
        List<String> sizes = productVariants.stream()
                .map(p -> p.getSize())
                .toList();
        ProductImage productImage = findProductImageByProductIdAndColor(id, color);
        return productMapper.toProductColorDto(product, sizes, productImage);
    }

    @Override
    public ProductColorSizeResponseDto getProductByColorAndSize(
            Long id, String color, String size) {
        Product product = findProduct(id);
        List<ProductVariant> productVariants = findProductByIdAndColor(id, color);
        ProductVariant productVariant = productVariants.stream()
                .filter(p -> size.equals(p.getSize()))
                .findAny()
                .orElseThrow(() -> new EntityNotFoundException("Product with id " + id
                        + " in color " + color + " and size " + size
                        + " wasn't found in database"));
        ProductImage productImages = findProductImageByProductIdAndColor(id, color);
        return productMapper.toProductColorSizeDto(product, productVariant, productImages);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, ProductUpdateRequestDto request) {
        Product product = findProduct(id);
        if (request.category() != null) {
            product.setCategory(request.category());
        }
        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.priceOld() != null) {
            product.setPriceOld(request.priceOld());
        }
        if (request.gender() != null) {
            product.setGender(request.gender());
        }
        if (request.season() != null) {
            product.setSeason(request.season());
        }
        if (request.material() != null) {
            product.setMaterial(request.material());
        }
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public ProductVariantDto updateProductVariant(Long id, ProductVariantUpdateRequestDto request) {
        ProductVariant productVariant = findProductVariant(id);
        Optional<ProductVariant> productVariantBySku =
                productVariantRepository.findBySku(request.sku());
        if (request.sku() != null && productVariantBySku.isPresent()
                && !productVariant.getId().equals(productVariantBySku.get().getId())) {
            throw new InputMismatchException("Sku " + request.sku()
                    + " already exists in database");
        }
        if (request.size() != null) {
            productVariant.setSize(request.size());
        }
        if (request.color() != null) {
            productVariant.setColor(request.color());
        }
        if (request.stockQty() != null) {
            productVariant.setStockQty(request.stockQty());
        }
        if (request.sku() != null) {
            productVariant.setSku(request.sku());
        }
        ProductVariant savedProductVariant = productVariantRepository.save(productVariant);
        return productMapper.toProductVariantDto(savedProductVariant);
    }

    @Override
    @Transactional
    public ProductImageDto updateProductImage(Long id, String color, ProductImageUpdateRequestDto request) {
        ProductImage productImage = findProductImageByProductIdAndColor(id, color);
        if (request.color() != null) {
            productImage.setColor(request.color());
        }
        if (request.mainUrl() != null) {
            productImage.setMainUrl(request.mainUrl());
        }
        if (request.urls() != null) {
            productImage.setUrls(request.urls());
        }
        ProductImage savedProductImage = productImageRepository.save(productImage);
        return productMapper.toProductImageDto(savedProductImage);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void deleteProductVariant(Long id) {
        ProductVariant productVariant = findProductVariant(id);
        productVariantRepository.delete(productVariant);
    }

    @Override
    @Transactional
    public void deleteProductImage(Long id) {
        ProductImage productImage = findProductImageById(id);
        productImageRepository.delete(productImage);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Product with id " + id
                        + " not found in database"));
    }

    private List<ProductVariant> findProductByIdAndColor(Long id, String color) {
        List<ProductVariant> productVariants =
                productVariantRepository.findByProductIdAndColor(id, color);
        if (productVariants.isEmpty()) {
            throw new EntityNotFoundException("Product with id " + id + " in color " + color
                    + " wasn't found in database");
        }
        return productVariants;
    }

    private ProductVariant findProductVariant(Long id) {
        return productVariantRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Product variant with id " + id
                        + " not found in database"));
    }

    private ProductImage findProductImageById(Long id) {
        return productImageRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Product image with id " + id
                        + " not found in database"));
    }

    private ProductImage findProductImageByProductIdAndColor(Long id, String color) {
        return productImageRepository.findByProductIdAndColor(id, color).orElseThrow(
                () -> new EntityNotFoundException("Product image with product id " + id
                + " and color " + color)
        );
    }
}
