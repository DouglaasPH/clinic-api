package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.auth.GoogleAuthDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints for managing authentication of the clinic")
public class AuthController {
    @Autowired
    private AuthService authService;

    // AUTHORIZATION: ANYONE
    @Operation(summary = "Login", description = "Login ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Succesfully"),
            @ApiResponse(responseCode = "400", description = "Email or password invalid")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login (@RequestBody @Valid LoginUserDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // AUTHORIZATION: ANYONE
    @PostMapping("/login/google")
    @Operation(summary = "Login via Google", description = "Login via Google.")
    public ResponseEntity<LoginResponseDto> loginGoogle(@RequestBody @Valid GoogleAuthDto dto) {
        LoginResponseDto response = authService.loginGoogle(dto);
        return ResponseEntity.ok(response);
    }
}
