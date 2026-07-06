package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.repositories.PatientRepository;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public List<Patient> findAll() { return repository.findAll(); }

    public Patient findById(Long id) {
        Optional<Patient> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException((id)));
    }

    public Patient insert(Patient obj) { return repository.save(obj); }
}
