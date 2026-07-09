package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.repositories.AppointmentRepository;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    // Business Rule: Admins can fetch all records, while patients and doctors can only fetch records linked to them.
    public List<Appointment> findAll(Authentication authentication) {
        String loggedEmail = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));

        if (isAdmin) {
            return repository.findAll();
        } else {
            return repository.findByPatientOrDoctorEmail(loggedEmail);
        }
    }

    public Appointment insert(Appointment obj) { return repository.save(obj); }

    public Appointment update(Long id, String newDiagnosis, AppointmentStatus newStatus) {
        try {
            Appointment entity = repository.getReferenceById(id);
            entity.setDiagnosis(newDiagnosis);
            entity.setStatus(newStatus);
            return repository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    public Appointment cancel(Long id) {
        try {
            Appointment entity = repository.getReferenceById(id);
            entity.setStatus(AppointmentStatus.CANCELED);
            return repository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }
}
