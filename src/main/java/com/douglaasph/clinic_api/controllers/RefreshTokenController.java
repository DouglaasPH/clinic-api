package com.douglaasph.clinic_api.controllers;


import com.douglaasph.clinic_api.controllers.dto.auth.LoginResponseDto;
import com.douglaasph.clinic_api.services.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/refresh-token")
public class RefreshTokenController {
    @Autowired
    private RefreshTokenService refreshTokenService;

    // AUTHORIZATION: ANYONE
    @Operation(summary = "Refresh token", description = "Refresh token ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Succesfully"),
            @ApiResponse(responseCode = "400", description = "Token invalid or expired")
    })
    @PostMapping("/refresh/{refreshToken}")
    public ResponseEntity<LoginResponseDto> refresh(@PathVariable String refreshToken) {
        return ResponseEntity.ok().body(refreshTokenService.refresh(refreshToken));
    }
}