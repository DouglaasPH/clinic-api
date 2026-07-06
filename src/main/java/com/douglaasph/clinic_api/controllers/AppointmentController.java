package com.douglaasph.clinic_api.controllers;

import com.douglaasph.clinic_api.controllers.dto.CreateAppointmentDto;
import com.douglaasph.clinic_api.controllers.dto.UpdateDiagnosisAppointmentDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.Patient;
import com.douglaasph.clinic_api.services.AppointmentService;
import com.douglaasph.clinic_api.services.DoctorService;
import com.douglaasph.clinic_api.services.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentController(AppointmentService appointmentService, PatientService patientService, DoctorService doctorService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<Appointment> insert(@RequestBody @Valid CreateAppointmentDto dto) {
            Doctor doctor = doctorService.findById(dto.doctor_id());
            Patient patient = patientService.findById(dto.patient_id());

            Appointment appointment = new Appointment(null, doctor, patient, dto.dateHour(), dto.status(), "");
            Appointment appointmentResponse = appointmentService.insert(appointment);
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appointmentResponse.getId()).toUri();
            return ResponseEntity.created(uri).body(appointmentResponse);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> findAll() {
        List<Appointment> response = appointmentService.findALl();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        Appointment response = appointmentService.cancel(id);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}/diagnosis")
    public ResponseEntity<Appointment> updateDiagnosis(@PathVariable Long id, @RequestBody @Valid UpdateDiagnosisAppointmentDto dto) {
        Appointment response = appointmentService.update(id, dto.diagnosis(), dto.status());
        return ResponseEntity.ok().body(response);
    }
}
