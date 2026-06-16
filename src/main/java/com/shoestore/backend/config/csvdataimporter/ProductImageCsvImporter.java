package com.shoestore.backend.config.csvdataimporter;

import com.shoestore.backend.model.Product;
import com.shoestore.backend.model.ProductImage;
import com.shoestore.backend.repository.ProductImageRepository;
import com.shoestore.backend.repository.ProductRepository;
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
public class ProductImageCsvImporter implements CommandLineRunner {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        Resource resource = new ClassPathResource("product_images.csv");

        if (productImageRepository.count() > 0) {
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
                    log.warn("Product image on row {} wasn't added to database. "
                            + "Not enough columns.", row);
                } else if (columns[0].isEmpty() || columns[1].isEmpty()) {
                    log.warn("Product image on row {} wasn't added to database. "
                            + "Product id, color can't be empty", row);
                } else {
                    try {
                        Long productId = Long.valueOf(columns[0]);
                        Optional<Product> productOptional = productRepository.findById(productId);
                        if (productOptional.isEmpty()) {
                            log.warn("Product image on row {} wasn't added to database. "
                                    + "Product with id {} not found.", row, columns[0]);
                            continue;
                        }
                        ProductImage productImage = new ProductImage()
                                .setProduct(productOptional.get()).setColor(columns[1])
                                .setUrlSmall(columns[2]).setUrlMedium(columns[3])
                                .setUrlLarge(columns[4]).setUrlOriginal(columns[5]);
                        productImageRepository.save(productImage);
                    } catch (NumberFormatException e) {
                        log.warn("Product image on row {} wasn't added to database. "
                                + "Product id must be valid number.", row);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import product images from CSV file");
        }
    }
}
