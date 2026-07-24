package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.user.UpdateUserDataDto;
import com.douglaasph.clinic_api.controllers.dto.user.UpdateUserPasswordDto;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "Endpoints for managing authenticated user profile and account details")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    @Autowired
    private UserService userService;

    // AUTHORIZATION: ANYONE
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves profile details for the currently authenticated user based on the provided JWT Bearer token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid Bearer token", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<User> me (Authentication authentication) {
        User response = userService.me(authentication.getName());
        return ResponseEntity.ok(response);
    }

    // AUTHORIZATION: ANYONE
    @PutMapping("/data")
    @Operation(
            summary = "Update personal data",
            description = "Updates the personal details (e.g., name, contact info) of the currently authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors in submitted data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid Bearer token", content = @Content)
    })
    public ResponseEntity<User> updateData (@RequestBody @Valid UpdateUserDataDto dto, Authentication authentication) {
        User response = userService.updateData(dto, authentication);
        return ResponseEntity.ok(response);
    }

    // AUTHORIZATION: ANYONE
    @PutMapping("/password")
    @Operation(
            summary = "Update password",
            description = "Changes the user password after verifying the current password against stored credentials."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation errors or incorrect current password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid Bearer token", content = @Content)
    })
    public ResponseEntity<User> updatePassword (@RequestBody @Valid UpdateUserPasswordDto dto, Authentication authentication) {
        User response = userService.updatePassword(dto, authentication);
        return ResponseEntity.ok(response);
    }
}
