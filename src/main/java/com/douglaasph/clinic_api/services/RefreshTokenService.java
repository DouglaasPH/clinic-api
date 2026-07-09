package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.RefreshToken;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.UserPrincipal;
import com.douglaasph.clinic_api.repositories.RefreshTokenRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public RefreshToken insert(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }

        // delete old (expired) tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        String newToken = UUID.randomUUID().toString();
        Instant validity = Instant.now().plus(7, ChronoUnit.DAYS); // 7 days
        RefreshToken refreshToken = new RefreshToken(null, newToken, user, validity);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please log in again.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}
