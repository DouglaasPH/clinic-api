package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.repositories.DoctorRepository;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public List<Doctor> findALl() { return repository.findAll(); }

    public Doctor findById(Long id) {
        Optional<Doctor> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException((id)));
    }

    public Doctor insert(Doctor obj) { return repository.save(obj); }
}
