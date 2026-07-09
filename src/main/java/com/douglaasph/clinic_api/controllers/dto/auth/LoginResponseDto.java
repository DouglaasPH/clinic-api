package com.douglaasph.clinic_api.controllers.dto.auth;

public record LoginResponseDto(boolean registered, String accessToken, String refreshToken) {}
