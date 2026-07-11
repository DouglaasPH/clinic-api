package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.repositories.RefreshTokenRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, JWTService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponseDto refresh(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        // verify expiration
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken = insert(refreshToken.getUser().getEmail());
            refreshTokenRepository.delete(refreshToken);
        }

        String accessToken = jwtService.generateToken(refreshToken.getUser().getEmail());

        return new LoginResponseDto(true, accessToken, refreshToken.getToken());
    }

    @Transactional
    public RefreshToken insert(String username) {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(("user not found")));

        // delete old (expired) tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        String newToken = UUID.randomUUID().toString();
        Instant validity = Instant.now().plus(7, ChronoUnit.DAYS); // 7 days
        RefreshToken refreshToken = new RefreshToken(null, newToken, user, validity);
        return refreshTokenRepository.save(refreshToken);
    }
}
