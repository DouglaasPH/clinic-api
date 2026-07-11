package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.XRayReport;
import com.douglaasph.clinic_api.models.entities.enums.ProcessingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface XRayReportRepository extends JpaRepository<XRayReport, Long> {
    boolean existsByAppointment_Patient_IdAndProcessingStatusNot(Long patientId, Integer processingStatus);

    Optional<XRayReport> findByAppointmentId(Long appointmentId);

    List<XRayReport> findAllByAppointment_Patient_IdAndReleasedToPatientTrue(Long patientId);
}
