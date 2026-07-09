package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.patient.user.email = :email OR a.doctor.user.email = :email")
    List<Appointment> findByPatientOrDoctorEmail(@Param("email") String email);
}
