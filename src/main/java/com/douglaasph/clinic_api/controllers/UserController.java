package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints for managing users of the clinic")
public class UserController {
    @Autowired
    private UserService userService;

    // AUTHORIZATION: ANYONE
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves profile details for the currently authenticated user based on the bearer token."
    )
    public ResponseEntity<User> me (Authentication authentication) {
        User response = userService.me(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
