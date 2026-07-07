package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import com.douglaasph.clinic_api.repositories.DoctorRepository;
import com.douglaasph.clinic_api.services.exceptions.DatabaseException;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public List<Doctor> findAll(String name, Specialties specialty) {
        return repository.findByOptionalFilters(name, specialty == null ? null : specialty.getCode());
    }

    public Doctor findById(Long id) {
        Optional<Doctor> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException((id)));
    }

    public Doctor insert(Doctor obj) {
        try {
            return repository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
