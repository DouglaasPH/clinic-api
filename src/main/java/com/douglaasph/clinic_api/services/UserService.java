package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.douglaasph.clinic_api.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User insert(User obj) { return repository.save(obj); }
}
