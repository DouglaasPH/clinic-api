package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.GoogleAuthDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.exceptions.TokenException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserRepository userRepository,
                       @Lazy AuthenticationManager authManager,
                       JWTService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponseDto login(LoginUserDto dto) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.email(),
                dto.password()));

        if (authentication.isAuthenticated()) {
            throw new BadCredentialsException("Email or password invalid.");
        }

        String accessToken = jwtService.generateToken(dto.email());
        RefreshToken refreshToken = refreshTokenService.insert(dto.email());

        return new LoginResponseDto(true, accessToken, refreshToken.getToken());
    }

    public Map<String, Object> checkGoogleUser(GoogleAuthDto dto) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(dto.googleToken());

            if (idToken == null) {
                throw new TokenException("Google token is invalid or has expired.");
            }

            Map<String, Object> result = new HashMap<>();
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            Optional<User> user = userRepository.findByEmail(email);

            if (user.isPresent()) {
                String accessToken = jwtService.generateToken(user.get().getEmail());
                RefreshToken refreshToken = refreshTokenService.insert(user.get().getEmail());
                return Map.of("registered", (Object) true, "auth", new LoginResponseDto(true, accessToken, refreshToken.getToken()));
            }

            result.put("registered", false);
            result.put("email", email);
            result.put("name", payload.get("name"));
            return result;
        } catch (TokenException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing social login", e);
        }
    }

    // username --> email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(("user not found")));

        return new UserPrincipal(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(("user not found")));
    }
}
