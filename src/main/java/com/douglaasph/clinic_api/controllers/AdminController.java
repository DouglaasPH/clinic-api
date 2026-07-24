package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.admin.AppointmentManagementAdminDto;
import com.douglaasph.clinic_api.controllers.dto.admin.DashboardAdminMetricsDto;
import com.douglaasph.clinic_api.controllers.dto.admin.EmployeesManagementMetricsDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.douglaasph.clinic_api.services.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/admin")
@Tag(name = "Admin", description = "Endpoints for managing the administrative portal")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Operation(
            summary = "Get admin dashboard metrics",
            description = "Retrieves general indicators, statistics, and summary data for the admin dashboard."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dashboard metrics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (missing or invalid token)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (requires ADMIN role)"
            )
    })
    @GetMapping("metrics/dashboard")
    public ResponseEntity<DashboardAdminMetricsDto> dashboardMetrics() {
        return ResponseEntity.ok(this.adminService.dashboardMetrics());
    }

    @Operation(
            summary = "Get employee management metrics",
            description = "Retrieves key performance indicators, active headcount, turn-over statistics, and administrative summary data related to clinic employees."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee management metrics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (missing or invalid JWT token)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (user lacks required ADMIN authority)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while processing employee metrics"
            )
    })
    @GetMapping("metrics/employees")
    public ResponseEntity<EmployeesManagementMetricsDto> employeesManagementMetrics() {
        return ResponseEntity.ok(this.adminService.employeesManagementMetrics());
    }

    @Operation(
            summary = "Get employee management",
            description = "Retrieves key performance indicators, active headcount, turn-over statistics, and administrative summary data related to clinic employees."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee management metrics retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized (missing or invalid JWT token)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (user lacks required ADMIN authority)"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while processing employee metrics"
            )
    })
    @GetMapping("management/employees")
    public ResponseEntity<Page<User>> employeesManagementWithPagination(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Position position,
            @RequestParam(defaultValue = "0") Integer page
    ) {
        return ResponseEntity.ok(this.adminService.findAllEmployeesForManagementWithPagination(name, position, page));
    }

    @Operation(
            summary = "List employees for management",
            description = "Retrieves a paginated list of clinic employees with optional filters by name and job position."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated list of employees retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Missing or invalid JWT token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - User lacks required ADMIN authority"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("management/appointments")
    public ResponseEntity<Page<AppointmentManagementAdminDto>> appointmentsManagementWithPagination(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") Integer page
    ) {
        return ResponseEntity.ok(this.adminService.findAllAppointmentsForManagementWithPagination(status, page));
    }
}