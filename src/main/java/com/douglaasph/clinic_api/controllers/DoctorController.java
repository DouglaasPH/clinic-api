package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.RegisterDoctorDto;
import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.services.DoctorService;
import com.douglaasph.clinic_api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(value = "/doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final UserService userService;

    public DoctorController(DoctorService doctorService, UserService userService) {
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Doctor> register (@RequestBody @Valid RegisterDoctorDto dto) {
        try {
            User user = new User(null, dto.user().name(), dto.user().email(), dto.user().password(), dto.user().role());
            User userResponse = userService.insert(user);
            Doctor doctor = new Doctor(null, dto.doctor().getCrm(), dto.doctor().getSpecialty(), userResponse);
            Doctor doctorResponse = doctorService.insert(doctor);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(doctorResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(doctorResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
