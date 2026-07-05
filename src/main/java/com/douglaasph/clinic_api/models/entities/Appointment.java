package com.douglaasph.clinic_api.models.entities;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

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
    private Patient patient;

    @Column(name = "date_hour", nullable = false)
    private LocalDateTime dateHour;

    @Column(nullable = false)
    private Integer appointmentStatus;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnosis;



    public Appointment () {}

    public Appointment(Long id, Doctor doctor, Patient patient, LocalDateTime dateHour, AppointmentStatus appointmentStatus, String diagnosis) {
        this.id = id;
        this.doctor = doctor;
        this.patient = patient;
        this.dateHour = dateHour;
        this.diagnosis = diagnosis;
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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
