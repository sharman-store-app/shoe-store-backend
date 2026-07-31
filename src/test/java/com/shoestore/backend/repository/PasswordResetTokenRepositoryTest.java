package com.shoestore.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.shoestore.backend.config.RepositoryTestData;
import com.shoestore.backend.model.PasswordResetToken;
import com.shoestore.backend.model.User;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByTokenReturnsPasswordResetToken() {
        User user = RepositoryTestData.user(entityManager, "token-user@example.com");
        PasswordResetToken token = new PasswordResetToken()
                .setUser(user)
                .setToken("reset-token-123")
                .setExpiresAt(Instant.now().plusSeconds(3600));
        entityManager.persistAndFlush(token);
        RepositoryTestData.flushAndClear(entityManager);

        assertThat(passwordResetTokenRepository.findByToken("reset-token-123"))
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getId)
                .isEqualTo(token.getId());
    }
}
