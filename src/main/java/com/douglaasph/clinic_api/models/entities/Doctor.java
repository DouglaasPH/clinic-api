package com.douglaasph.clinic_api.models.entities;

import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "tb_doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String crm;

    @Column(nullable = false)
    private Specialties specialty;

    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Doctor () {}

    public Doctor(Long id, String crm, Specialties specialty) {
        this.id = id;
        this.crm = crm;
        this.specialty = specialty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public Specialties getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialties specialty) {
        this.specialty = specialty;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return Objects.equals(id, doctor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
