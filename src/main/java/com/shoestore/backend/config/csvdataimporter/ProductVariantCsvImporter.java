package com.shoestore.backend.config.csvdataimporter;

import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.model.ProductVariant;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductRepository;
import com.shoestore.backend.repository.ProductVariantRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class ProductVariantCsvImporter implements CommandLineRunner {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Override
    public void run(String... args) throws Exception {
        Resource resource = new ClassPathResource("product_variants.csv");

        if (productVariantRepository.count() > 0) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            reader.readLine();
            String line;
            int row = 1;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";");
                row++;
                if (columns.length != 6) {
                    log.warn("Product variant on row {} wasn't added to database. "
                            + "Not enough columns.", row);
                } else if (columns[0].isEmpty() || columns[1].isEmpty() || columns[2].isEmpty()
                        || columns[3].isEmpty() || columns[4].isEmpty() || columns[5].isEmpty()) {
                    log.warn("Product variant on row {} wasn't added to database. "
                            + "Product id, color, size, stock_qty, sku, and product image "
                            + "can't be empty", row);
                } else if (productVariantRepository.findBySku(columns[4]).isPresent()) {
                    log.warn("Product variant on row {} wasn't added to database. "
                                    + "Product variant with sku {} already exists in database",
                            row, columns[4]);
                } else {
                    try {
                        Long productId = Long.valueOf(columns[0]);
                        Integer stockQty = Integer.valueOf(columns[3]);
                        Optional<Product> productOptional = productRepository.findById(productId);
                        Optional<ProductImage> productImageOptional =
                                productImageRepository.findById(Long.valueOf(columns[5]));
                        if (productOptional.isEmpty()) {
                            log.warn("Product variant on row {} wasn't added to database. "
                                    + "Product with id {} not found.", row, columns[0]);
                            continue;
                        }
                        if (productImageOptional.isEmpty()) {
                            log.warn("Product variant on row {} wasn't added to database. "
                                    + "Product image with id {} not found.", row, columns[5]);
                            continue;
                        }
                        ProductVariant productVariant = new ProductVariant()
                                .setProduct(productOptional.get()).setSize(columns[2])
                                .setColor(columns[1]).setStockQty(stockQty).setSku(columns[4])
                                .setProductImage(productImageOptional.get());
                        productVariantRepository.save(productVariant);
                    } catch (NumberFormatException e) {
                        log.warn("Product variant on row {} wasn't added to database. "
                                + "Product id and stock_qty must be valid numbers.", row);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import product variants from CSV file");
        }
    }
}
