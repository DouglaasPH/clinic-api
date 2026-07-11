package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityFactory.class)
class RefreshTokenRepositoryTest {
    @Autowired
    RefreshTokenRepository repository;

    @Autowired
    TestEntityFactory testEntityFactory;

    @Test
    @DisplayName("Should successfully return the refresh token when it exists in the database")
    void findByTokenCase1() {
        UserDto data = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        User user = this.testEntityFactory.createUser(data, Roles.ADMIN);

        String token = "tokenName";
        this.testEntityFactory.createRefreshToken(user, token);

        Optional<RefreshToken> result = this.repository.findByToken(token);

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should return an empty Optional when the refresh token does not exist")
    void findByTokenCase2() {
        String token = "tokenName";
        Optional<RefreshToken> result = this.repository.findByToken(token);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should successfully delete refresh token when searching by a valid user ID")
    void deleteByUserIdCase3() {
        UserDto data = new UserDto("Douglas Phelipe", "example@gmail.com", "1234");
        User user = this.testEntityFactory.createUser(data, Roles.ADMIN);
        String token = "tokenName";
        this.testEntityFactory.createRefreshToken(user, token);

        this.repository.deleteByUserId(user.getId());

        Optional<RefreshToken> result = this.repository.findByToken(token);

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should not throw an exception or delete anything when user ID does not exist")
    void deleteByUserIdCase4() {
        assertDoesNotThrow(() -> this.repository.deleteByUserId(999L));
    }
}