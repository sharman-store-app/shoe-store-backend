package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmailReturnsUser() {
        User user = RepositoryTestData.user(entityManager, "repo-user@example.com");
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(userRepository.findByEmail("repo-user@example.com"))
                .isPresent()
                .get()
                .extracting(User::getId)
                .isEqualTo(user.getId());
    }

    @Test
    void findByEmailIgnoresSoftDeletedUser() {
        User user = RepositoryTestData.user(entityManager, "deleted-user@example.com");
        userRepository.delete(user);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(userRepository.findByEmail("deleted-user@example.com")).isEmpty();
    }
}
