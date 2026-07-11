package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.auth.GoogleAuthDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.controllers.dto.auth.LoginUserDto;
import com.douglaasph.clinic_api.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
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
    @PostMapping("/google/check")
    @Operation(summary = "Check Google User", description = "Checks if the Google user is already registered in the system.")
    public ResponseEntity<?> checkGoogleUser(@RequestBody @Valid GoogleAuthDto dto) {
        Map<String, Object> response = authService.checkGoogleUser(dto);
        return ResponseEntity.ok(response);
    }
}
