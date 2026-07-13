package com.douglaasph.clinic_api.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "x_ray_report")
public class XRayReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String s3Key;

    @Column(nullable = false)
    private Integer processingStatus;

    @Column(nullable = true)
    private String aiResult;

    @Column(nullable = true)
    private String finalMedicalDiagnosis;

    @Column(nullable = false)
    private boolean releasedToPatient = false;

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    public boolean isReleasedToPatient() {
        return this.releasedToPatient;
    }
}
