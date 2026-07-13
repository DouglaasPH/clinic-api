package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.RefreshTokenRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RefreshTokenServiceTest {
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should successfully refresh and return new access token when current refresh token is valid")
    void refreshCase1() {
        User userMock = new User(null, "Douglas Phelipe", "example@gmail.com", "mocked-encode", Roles.PATIENT);
        RefreshToken refreshTokenMock = new RefreshToken(null, "mocked-refresh-token", userMock, Instant.now().plus(1, ChronoUnit.DAYS));

        Mockito.when(refreshTokenRepository.findByToken(any(String.class))).thenReturn(Optional.of(refreshTokenMock));

        Mockito.when(jwtService.generateToken(any(String.class))).thenReturn("mocked-access-token");

        LoginResponseDto response = refreshTokenService.refresh("mocked-refresh-token");

        assertNotNull(response);
        assertTrue(response.registered());
        assertEquals("mocked-access-token", response.accessToken());
        assertEquals("mocked-refresh-token", response.refreshToken());
    }

    @Test
    @DisplayName("Should generate a new refresh token and access token when the current refresh token is expired")
    void refreshCase2() {
        User userMock = new User(null, "Douglas Phelipe", "example@gmail.com", "mocked-encode", Roles.PATIENT);
        RefreshToken expiredTokenMock = new RefreshToken(null, "expired-refresh-token", userMock, Instant.now().minus(1, ChronoUnit.DAYS));
        RefreshToken newTokenMock = new RefreshToken(null, "new-refresh-token", userMock, Instant.now().plus(7, ChronoUnit.DAYS));

        Mockito.when(refreshTokenRepository.findByToken("expired-refresh-token")).thenReturn(Optional.of(expiredTokenMock));
        Mockito.when(userRepository.findByEmail("example@gmail.com")).thenReturn(Optional.of(userMock));
        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newTokenMock);
        Mockito.when(jwtService.generateToken("example@gmail.com")).thenReturn("mocked-access-token");

        LoginResponseDto response = refreshTokenService.refresh("expired-refresh-token");

        assertNotNull(response);
        assertTrue(response.registered());
        assertEquals("mocked-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());

        Mockito.verify(refreshTokenRepository, Mockito.times(1)).deleteByUserId(userMock.getId());
        Mockito.verify(refreshTokenRepository, Mockito.times(1)).delete(expiredTokenMock);
    }

    @Test
    @DisplayName("Should insert and return a new refresh token when user exists")
    void insertCase1() {
        User userMock = new User(null, "Douglas Phelipe", "example@gmail.com", "mocked-encode", Roles.PATIENT);

        Mockito.when(userRepository.findByEmail("example@gmail.com")).thenReturn(Optional.of(userMock));

        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.insert("example@gmail.com");

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(userMock, result.getUser());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));

        Mockito.verify(refreshTokenRepository, Mockito.times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user email does not exist")
    void insertCase2() {
        Mockito.when(userRepository.findByEmail(any(String.class)))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            refreshTokenService.insert("example@gmail.com");
        });

        assertEquals("User not found", exception.getMessage());
        Mockito.verifyNoInteractions(refreshTokenRepository);
    }
}