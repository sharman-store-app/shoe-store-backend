package com.shoestore.backend.repository;

import static com.shoestore.backend.config.RepositoryTestData.flushAndClear;
import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CountryRepositoryTest {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdReturnsCountry() {
        RepositoryTestData.flushAndClear(entityManager);
        flushAndClear(entityManager);

        assertThat(countryRepository.findById("PL"))
                .isPresent()
                .get()
                .extracting("name")
                .isEqualTo("Poland");
    }
}
