package com.douglaasph.clinic_api.controllers.dto;

import com.douglaasph.clinic_api.models.entities.Doctor;

public record RegisterDoctorDto(
        UserDto user,
        Doctor doctor
) {}
