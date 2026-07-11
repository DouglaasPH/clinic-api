package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityFactory.class)
class UserRepositoryTest {
    @Autowired
    UserRepository repository;

    @Autowired
    TestEntityFactory testEntityFactory;

    @Test
    @DisplayName("Should get User succesfully from DB")
    void findByEmailCase1() {
        String email = "example@gmail.com";
        UserDto data = new UserDto("Douglas Phelipe", email, "1234");
        this.testEntityFactory.createUser(data, Roles.ADMIN);

        Optional<User> result = this.repository.findByEmail(email);

        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should not get User succesfully from DB when user not exists")
    void findByEmailCase2() {
        String email = "example@gmail.com";

        Optional<User> result = this.repository.findByEmail(email);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should exists verify User succesfully from DB")
    void existsByEmailCase1() {
        String email = "example@gmail.com";
        UserDto data = new UserDto("Douglas Phelipe", email, "1234");
        this.testEntityFactory.createUser(data, Roles.ADMIN);

        boolean result = this.repository.existsByEmail(email);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not exists verify User succesfully from DB when user not exists")
    void existsByEmailCase2() {
        String email = "example@gmail.com";
        boolean result = this.repository.existsByEmail(email);

        assertThat(result).isFalse();
    }
}