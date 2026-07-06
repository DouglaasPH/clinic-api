package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.repositories.AppointmentRepository;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> findALl() { return repository.findAll(); }

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
