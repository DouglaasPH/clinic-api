package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.GoogleAuthDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.exceptions.TokenException;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    @Spy
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void loginCase1() {
        LoginUserDto dto = new LoginUserDto("usuario@gmail.com", "1234");

        Authentication authMock = Mockito.mock(Authentication.class);
        Mockito.when(authMock.isAuthenticated()).thenReturn(true);

        Mockito.when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);

        Mockito.when(jwtService.generateToken("usuario@gmail.com")).thenReturn("mocked-access-token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mocked-refresh-token");
        Mockito.when(refreshTokenService.insert("usuario@gmail.com")).thenReturn(mockRefreshToken);

        LoginResponseDto response = authService.login(dto);

        assertNotNull(response);
        assertTrue(response.registered());
        assertEquals("mocked-access-token", response.accessToken());
        assertEquals("mocked-refresh-token", response.refreshToken());

        Mockito.verify(refreshTokenService, Mockito.times(1)).insert("usuario@gmail.com");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when authentication fails")
    void loginCase2() {
        LoginUserDto dto = new LoginUserDto("example@gmail.com", "invalidPassword");

        Authentication authMockFalse = Mockito.mock(Authentication.class);
        Mockito.when(authMockFalse.isAuthenticated()).thenReturn(false);

        Mockito.when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMockFalse);

        assertThrows(BadCredentialsException.class, () -> authService.login(dto));

        Mockito.verifyNoInteractions(jwtService);
        Mockito.verifyNoInteractions(refreshTokenService);

    }

    @Test
    @DisplayName("Should authenticate existing Google user successfully")
    void checkGoogleUserCase1() throws GeneralSecurityException, IOException {
        GoogleAuthDto dto = new GoogleAuthDto("mocked-google-token");

        GoogleIdTokenVerifier verifierMock = Mockito.mock(GoogleIdTokenVerifier.class);
        GoogleIdToken idTokenMock = Mockito.mock(GoogleIdToken.class);
        GoogleIdToken.Payload payloadMock = Mockito.mock(GoogleIdToken.Payload.class);

        Mockito.when(verifierMock.verify(Mockito.anyString())).thenReturn(idTokenMock);
        Mockito.when(idTokenMock.getPayload()).thenReturn(payloadMock);
        Mockito.when(payloadMock.getEmail()).thenReturn("patient@gmail.com");

        Mockito.doReturn(verifierMock).when(authService).getGoogleIdTokenVerifier();

        User existingUser = new User();
        existingUser.setEmail("patient@gmail.com");
        Mockito.when(userRepository.findByEmail("patient@gmail.com")).thenReturn(Optional.of(existingUser));

        Mockito.when(jwtService.generateToken("patient@gmail.com")).thenReturn("mocked-access-token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("mocked-refresh-token");
        Mockito.when(refreshTokenService.insert("patient@gmail.com")).thenReturn(mockRefreshToken);

        Map<String, Object> result = authService.checkGoogleUser(dto);

        assertNotNull(result);
        assertEquals(true, result.get("registered"));

        LoginResponseDto authResponse = (LoginResponseDto) result.get("auth");
        assertNotNull(authResponse);
        assertEquals("mocked-access-token", authResponse.accessToken());
        assertEquals("mocked-refresh-token", authResponse.refreshToken());

        Mockito.verify(jwtService, Mockito.times(1)).generateToken("patient@gmail.com");
        Mockito.verify(refreshTokenService, Mockito.times(1)).insert("patient@gmail.com");
    }

    @Test
    @DisplayName("Should throw TokenException when Google token is invalid or expired")
    void checkGoogleUserCase2() throws GeneralSecurityException, IOException {
        GoogleAuthDto dto = new GoogleAuthDto("mocked-google-token");

        GoogleIdTokenVerifier verifierMock = Mockito.mock(GoogleIdTokenVerifier.class);
        Mockito.when(verifierMock.verify(Mockito.anyString())).thenReturn(null);

        Mockito.doReturn(verifierMock).when(authService).getGoogleIdTokenVerifier();

        assertThrows(TokenException.class, () -> authService.checkGoogleUser(dto));

        Mockito.verifyNoInteractions(userRepository);
        Mockito.verifyNoInteractions(jwtService);
        Mockito.verifyNoInteractions(refreshTokenService);
    }

}