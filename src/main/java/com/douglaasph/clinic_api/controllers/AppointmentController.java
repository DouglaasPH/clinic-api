package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.CreateAppointmentDto;
import com.douglaasph.clinic_api.controllers.dto.UpdateDiagnosisAppointmentDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.services.AppointmentService;
import com.douglaasph.clinic_api.services.DoctorService;
import com.douglaasph.clinic_api.services.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/appointment")
@Tag(name = "Doctors", description = "Endpoints for managing doctors of the clinic")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentController(AppointmentService appointmentService, PatientService patientService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @Operation(summary = "Insert appointment", description = "Valid data and add appointment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Inserted appointment with success"),
            @ApiResponse(responseCode = "400", description = "Invalid data (validation failure)")
    })
    @PostMapping
    public ResponseEntity<Appointment> insert(@RequestBody @Valid CreateAppointmentDto dto) {
            Doctor doctor = doctorService.findById(dto.doctor_id());
            Patient patient = patientService.findById(dto.patient_id());

            Appointment appointment = new Appointment(null, doctor, patient, dto.dateHour(), dto.status(), "");
            Appointment appointmentResponse = appointmentService.insert(appointment);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appointmentResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(appointmentResponse);
    }

    @Operation(summary = "Find all appointments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "All inserted appointments")
    })
    @GetMapping
    public ResponseEntity<List<Appointment>> findAll() {
        List<Appointment> response = appointmentService.findALl();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Cancel appointment", description = "Cancel appointment by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Canceled appointment with success"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @GetMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        Appointment response = appointmentService.cancel(id);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Insert or update Diagnosis and update status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Updated appointment with success"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @GetMapping("/{id}/diagnosis")
    public ResponseEntity<Appointment> updateDiagnosis(@PathVariable Long id, @RequestBody @Valid UpdateDiagnosisAppointmentDto dto) {
        Appointment response = appointmentService.update(id, dto.diagnosis(), dto.status());
        return ResponseEntity.ok().body(response);
    }
}
