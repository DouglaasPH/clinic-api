package com.douglaasph.clinic_api.controllers.dto;

public record LoginResponseDto(boolean registered, String accessToken, String refreshToken) {}
