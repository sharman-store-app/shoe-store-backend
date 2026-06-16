package com.shoestore.backend.mapper;

import com.shoestore.backend.config.MapperConfig;
import com.shoestore.backend.dto.product.CreateProductRequestDto;
import com.shoestore.backend.dto.product.ProductColorResponseDto;
import com.shoestore.backend.dto.product.ProductColorSizeResponseDto;
import com.shoestore.backend.dto.product.ProductDto;
import com.shoestore.backend.dto.product.ProductResponseDto;
import com.shoestore.backend.dto.product.image.CreateProductImageRequestDto;
import com.shoestore.backend.dto.product.image.ProductImageDto;
import com.shoestore.backend.dto.product.image.ProductImageResponseDto;
import com.shoestore.backend.dto.product.variant.CreateProductVariantRequestDto;
import com.shoestore.backend.dto.product.variant.ProductVariantDto;
import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Product toEntity(CreateProductRequestDto request);

    ProductDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ProductVariant toProductVariant(CreateProductVariantRequestDto request);

    @Mapping(target = "productId", source = "product.id")
    ProductVariantDto toProductVariantDto(ProductVariant productVariant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ProductImage toProductImage(CreateProductImageRequestDto request);

    @Mapping(target = "productId", source = "product.id")
    ProductImageDto toProductImageDto(ProductImage savedProductImage);

    ProductImageResponseDto toProductDto(ProductImage productImage);

    default ProductResponseDto toProductDto(
            Product product, List<ProductImage> productImageList,
            List<ProductVariant> productVariantList) {

        List<String> colors = productVariantList.stream()
                .filter(productVariant ->
                        productVariant.getProduct().getId().equals(product.getId()))
                .map(ProductVariant::getColor)
                .distinct()
                .toList();

        List<ProductImageResponseDto> productImageResponseDtos =
                mapImageList(productImageList, product);

        return new ProductResponseDto(product.getId(),
                product.getCategory(), product.getName(), product.getDescription(),
                product.getPrice(), product.getPriceOld(), product.getGender(), product.getSeason(),
                product.getMaterial(), colors, product.getCreatedAt(), productImageResponseDtos);
    }

    default ProductColorResponseDto toProductColorDto(
            Product product, List<String> sizes, List<ProductImage> productImages) {

        List<ProductImageResponseDto> productImagesResponseDto = productImages.stream()
                .map(this::toProductDto)
                .toList();

        return new ProductColorResponseDto(product.getId(),
                product.getCategory(), product.getName(), product.getDescription(),
                product.getPrice(), product.getPriceOld(), product.getGender(), product.getSeason(),
                product.getMaterial(), sizes, product.getCreatedAt(), productImagesResponseDto);
    }

    default ProductColorSizeResponseDto toProductColorSizeDto(
            Product product, ProductVariant productVariant, List<ProductImage> productImageList) {

        List<ProductImageResponseDto> productImageResponseDto =
                mapImageList(productImageList, product);

        return new ProductColorSizeResponseDto(product.getId(),
                product.getCategory(), product.getName(), product.getDescription(),
                product.getPrice(), product.getPriceOld(), product.getGender(), product.getSeason(),
                product.getMaterial(), productVariant.getStockQty(), product.getCreatedAt(),
                productImageResponseDto);
    }

    private List<ProductImageResponseDto> mapImageList(
            List<ProductImage> productImages, Product product) {
        return productImages.stream()
                .filter(productImage ->
                        productImage.getProduct().getId().equals(product.getId()))
                .map(this::toProductDto)
                .toList();
    }

}
