package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.controllers.dto.admin.AppointmentManagementAdminDto;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.User;
import com.douglaasph.clinic_api.models.entities.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.patient.user.email = :email OR a.employee.user.email = :email")
    List<Appointment> findByPatientOrEmployeeEmail(@Param("email") String email);

    List<Appointment> findByAppointmentStatusAndDateHourAfter(int appointmentStatus, LocalDateTime dateHour);

    @Query("""
        SELECT new com.douglaasph.clinic_api.controllers.dto.admin.AppointmentManagementAdminDto(
            a.id,
            a.dateHour,
            a.appointmentStatus,
            a.appointmentType,
            eUser.name,
            pUser.name
        )
        FROM Appointment a
        LEFT JOIN a.employee e
        LEFT JOIN e.user eUser
        LEFT JOIN a.patient p
        LEFT JOIN p.user pUser
        WHERE (:appointmentStatus IS NULL OR a.appointmentStatus = :appointmentStatus)
    """)
    Page<AppointmentManagementAdminDto> findAllAppointmentsManagement(
            @Param("appointmentStatus") Integer appointmentStatus,
            Pageable pageable
    );
}
