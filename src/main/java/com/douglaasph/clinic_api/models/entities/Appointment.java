package com.douglaasph.clinic_api.models.entities;

import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private Patient patient;

    @Column(name = "date_hour", nullable = false)
    private LocalDateTime dateHour;

    @Column(nullable = false)
    private Integer appointmentStatus;

    @Column(nullable = false)
    private Integer appointmentType;

    public Appointment(Long id, Employee employee, Patient patient, LocalDateTime dateHour, AppointmentStatus appointmentStatus, AppointmentType appointmentType) {
        this.id = id;
        this.employee = employee;
        this.patient = patient;
        this.dateHour = dateHour;
        setStatus(appointmentStatus);
        setType(appointmentType);
    }

    public AppointmentStatus getStatus() {
        return AppointmentStatus.valueOf(appointmentStatus);
    }

    public void setStatus(AppointmentStatus appointmentStatus) {
        if (appointmentStatus != null) {
            this.appointmentStatus = appointmentStatus.getCode();
        }
    }

    public AppointmentType getType() {
        return AppointmentType.valueOf(appointmentType);
    }

    public void setType(AppointmentType appointmentType) {
        if (appointmentType != null) {
            this.appointmentType = appointmentType.getCode();
        }
    }
}
