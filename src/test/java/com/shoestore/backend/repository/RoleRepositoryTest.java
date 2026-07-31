package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByRoleNameReturnsRole() {
        RepositoryTestData.customerRole(entityManager);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(roleRepository.findByRoleName(RoleName.CUSTOMER))
                .isPresent()
                .get()
                .extracting("authority")
                .isEqualTo("ROLE_CUSTOMER");
    }
}
