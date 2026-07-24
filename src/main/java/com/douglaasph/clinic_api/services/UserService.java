package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.user.UpdateUserDataDto;
import com.douglaasph.clinic_api.controllers.dto.user.UpdateUserPasswordDto;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.models.entities.*;
import com.douglaasph.clinic_api.models.entities.enums.Roles;
import com.douglaasph.clinic_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;



    public User me(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public User updateData(UpdateUserDataDto dto, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));

        if (user.getRole() == Roles.PATIENT) {
            if (dto.cpf() != null) {
                user.getPatient().setCpf(dto.cpf());
            }
            if (dto.phone() != null) {
                user.getPatient().setPhone(dto.phone());
            }
        }

        return userRepository.save(user);
    }

    public User updatePassword(UpdateUserPasswordDto dto, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new ResourceNotFoundException("User", "email", authentication.getName()));
        System.out.println(dto.currentPassword());
        System.out.println(dto.newPassword());
        System.out.println("Senha enviada no DTO: " + dto.currentPassword());
        System.out.println("Hash vindo do Banco: " + user.getPassword());
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("The current password is different from the user's.");
        }

        if (dto.currentPassword().equals(dto.newPassword())) {
            throw new BadCredentialsException("The new password must be different.");
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));

        return userRepository.save(user);
    }
}
