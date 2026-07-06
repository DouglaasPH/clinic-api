package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.repositories.AppointmentRepository;
import com.douglaasph.clinic_api.services.exceptions.DatabaseException;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository repository;

    public AppointmentService(AppointmentRepository repository) {
        this.repository = repository;
    }

    public List<Appointment> findALl() { return repository.findAll(); }

    public Appointment insert(Appointment obj) { return repository.save(obj); }

    public Appointment update(Long id, Appointment obj) {
        try {
            Appointment entity = repository.getReferenceById(id);
            updateData(entity, obj);
            return repository.save(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    private void updateData(Appointment entity, Appointment obj) {
        entity.setDoctor(obj.getDoctor());
        entity.setPatient(obj.getPatient());
        entity.setDateHour(obj.getDateHour());
        entity.setStatus(obj.getStatus());
    }
}
