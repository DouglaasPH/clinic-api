package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.UserPrincipal;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.douglaasph.clinic_api.exceptions.DatabaseException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AuthenticationManager authManager;

    public UserService(UserRepository repository, @Lazy AuthenticationManager authManager) {
        this.repository = repository;
        this.authManager = authManager;
    }

    public User insert(User obj) {
        try {
            obj.setPassword(encoder.encode(obj.getPassword()));
            return repository.save(obj);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    // username --> email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email);

        if (user == null) {
            System.out.println("User Not Found");
            throw new UsernameNotFoundException("user not found");
        }

        return new UserPrincipal(user);
    }

    public Boolean verify(LoginUserDto user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.email(), user.password()));
        return authentication.isAuthenticated();
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
