package com.douglaasph.clinic_api.controllers.dto.user;

public record UpdateUserPasswordDto(String currentPassword, String newPassword) {
}
