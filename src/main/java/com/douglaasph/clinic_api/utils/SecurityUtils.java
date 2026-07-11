package com.douglaasph.clinic_api.utils;

import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.douglaasph.clinic_api.services.PatientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("securityUtils")
public class SecurityUtils {
    private final UserRepository userRepository;
    private final PatientService patientService;

    public SecurityUtils(UserRepository userRepository, PatientService patientService) {
        this.userRepository = userRepository;
        this.patientService = patientService;
    }

    public boolean isEmployeeDoctor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException(("user not found")));

        return user.getEmployee().getPosition() == Position.DOCTOR;
    }

    public boolean isEmployeeTechnical(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException(("user not found")));

        return user.getEmployee().getPosition() == Position.TECHNICAL;
    }

    public boolean isAdminOrPatientThemSelves(Authentication authentication, Long patientId) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException(("user not found")));

        return user.getRole().getCode() == 1 ||
                (user.getRole().getCode() == 3 &&
                        Objects.equals(patientService.findById(patientId).getId(), user.getPatient().getId()));
    }
}
