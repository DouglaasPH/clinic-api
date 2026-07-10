package com.douglaasph.clinic_api.controllers.dto.employee;

import com.douglaasph.clinic_api.controllers.dto.user.UserDto;

public record RegisterEmployeeDto(
        UserDto user,
        EmployeeDto employee
) {}
