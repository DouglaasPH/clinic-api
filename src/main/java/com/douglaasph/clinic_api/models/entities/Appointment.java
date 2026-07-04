package com.douglaasph.clinic_api.models.entities;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Long patient_id;

    @Column(name = "date_hour", nullable = false)
    private LocalDateTime dateHour;

    private Integer appointmentStatus;

    public Appointment () {}

    public Appointment(Long id, Doctor doctor, Long patient_id, LocalDateTime dateHour, AppointmentStatus appointmentStatus) {
        this.id = id;
        this.doctor = doctor;
        this.patient_id = patient_id;
        this.dateHour = dateHour;
        setStatus(appointmentStatus);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public LocalDateTime getDateHour() {
        return dateHour;
    }

    public void setDateHour(LocalDateTime dateHour) {
        this.dateHour = dateHour;
    }

    public AppointmentStatus getStatus() {
        return AppointmentStatus.valueOf(appointmentStatus);
    }

    public void setStatus(AppointmentStatus appointmentStatus) {
        if (appointmentStatus != null) {
            this.appointmentStatus = appointmentStatus.getCode();
        }
    }
}
