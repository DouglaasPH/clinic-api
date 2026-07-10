package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.models.entities.Employee;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/employee")
@Tag(name = "Employee", description = "Endpoints for managing employees of the clinic")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // AUTHORIZATION: ADMIN or PATIENT
    @Operation(summary = "Find all by name or position or both")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All doctors found")
    })
    @GetMapping
    public ResponseEntity<List<Employee>> findAllByNameOrPosition (@RequestParam(required = false) String name, @RequestParam(required = false) Position position) {
        List<Employee> response = employeeService.findAll(name, position);
        return ResponseEntity.ok().body(response);
    }

    // AUTHORIZATION: ANY ROLE (authenticated only)
    @Operation(summary = "Find doctor by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Doctor found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Employee> findById (@PathVariable Long id) {
        Employee response = employeeService.findById(id);
        return ResponseEntity.ok().body(response);
    }
}
