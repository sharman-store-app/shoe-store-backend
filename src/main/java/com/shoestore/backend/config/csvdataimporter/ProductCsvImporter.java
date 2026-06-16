package com.shoestore.backend.config.csvdataimporter;

import com.shoestore.backend.model.Product;
import com.shoestore.backend.repository.ProductRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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
@Order(1)
public class ProductCsvImporter implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        Resource resource = new ClassPathResource("products.csv");

        if (productRepository.count() > 0) {
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
                if (columns.length != 8) {
                    log.warn("Product on row {} wasn't added to database. "
                            + "Not enough columns.", row);
                } else if (columns[0].isEmpty() || columns[1].isEmpty() || columns[3].isEmpty()) {
                    log.warn("Product on row {} wasn't added to database."
                            + "Category, name and price can't be empty", row);
                } else {
                    try {
                        new BigDecimal(columns[3]);
                        if (!columns[4].isEmpty()) {
                            new BigDecimal(columns[4]);
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Product on row {} wasn't added to database. "
                                + "Price and old price must be valid numbers.", row);
                        continue;
                    }
                    Product product = new Product();
                    product.setName(columns[0]).setCategory(columns[1]).setDescription(columns[2])
                            .setPrice(new BigDecimal(columns[3])).setGender(columns[5])
                            .setSeason(columns[6]).setMaterial(columns[7]);
                    if (!columns[4].isEmpty()) {
                        product.setPriceOld(new BigDecimal(columns[4]));
                    }
                    productRepository.save(product);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to import products from CSV file");
        }
    }
}
