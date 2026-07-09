package com.douglaasph.clinic_api.controllers.dto.doctor;

import com.douglaasph.clinic_api.controllers.dto.user.UserDto;
import com.douglaasph.clinic_api.models.entities.Doctor;

public record RegisterDoctorDto(
        UserDto user,
        Doctor doctor
) {}
