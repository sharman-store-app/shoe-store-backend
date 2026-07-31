package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.Session;
import com.shoestore.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndFindByIdReturnsSession() {
        User user = RepositoryTestData.user(entityManager, "session-user@example.com");
        Session session = RepositoryTestData.session(entityManager, user);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(sessionRepository.findById(session.getId()))
                .isPresent()
                .get()
                .extracting(Session::getId)
                .isEqualTo(session.getId());
    }
}
