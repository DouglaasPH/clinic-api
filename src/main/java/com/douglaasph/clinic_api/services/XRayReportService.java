package com.douglaasph.clinic_api.services;

import com.douglaasph.clinic_api.config.aws.QueueGateway;
import com.douglaasph.clinic_api.config.aws.StorageGateway;
import com.douglaasph.clinic_api.exceptions.ResourceNotFoundException;
import com.douglaasph.clinic_api.models.entities.Appointment;
import com.douglaasph.clinic_api.models.entities.XRayReport;
import com.douglaasph.clinic_api.models.entities.enums.ProcessingStatus;
import com.douglaasph.clinic_api.repositories.AppointmentRepository;
import com.douglaasph.clinic_api.repositories.UserRepository;
import com.douglaasph.clinic_api.repositories.XRayReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class XRayReportService {
    private final XRayReportRepository xRayReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final StorageGateway storageGateway;
    private final QueueGateway queueGateway;

    public XRayReportService(XRayReportRepository xRayReportRepository,
                             StorageGateway storageGateway,
                             QueueGateway queueGateway, AppointmentRepository appointmentRepository, UserRepository userRepository) {
        this.xRayReportRepository = xRayReportRepository;
        this.storageGateway = storageGateway;
        this.queueGateway = queueGateway;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createReportAndGenerateUploadUrl(Appointment appointment) {
        // Define o caminho físico (chave do objeto no Storage)
        String s3Key = "exams/" + UUID.randomUUID() + ".png";

        // Cria e persiste a entidade no Banco via JPA
        XRayReport report = new XRayReport();
        report.setAppointment(appointment);
        report.setS3Key(s3Key);
        report.setProcessingStatus(ProcessingStatus.AWAITING_AI.getCode());
        XRayReport savedReport = xRayReportRepository.save(report);

        String presignedUrl = storageGateway.generatePresignedUploadUrl(s3Key);
        queueGateway.sendExamNotification(savedReport.getId(), s3Key);

        return presignedUrl;
    }

    @Transactional
    public XRayReport reviewDoctor(Long appointmentId, String finalDoctorDiagnosis) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(appointmentId));

        XRayReport xRayReport = xRayReportRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new ResourceNotFoundException("Resource not found."));

        xRayReport.setFinalMedicalDiagnosis(finalDoctorDiagnosis);
        xRayReport.setProcessingStatus(4);
        appointment.setAppointmentStatus(4);
        xRayReport.setReleasedToPatient(true);
        appointmentRepository.save(appointment);
        xRayReportRepository.save(xRayReport);

        return xRayReport;
    }

    @Transactional
    public List<XRayReport> findAllByPatientIdAndReleasedToPatientTrue(String email) {
        Long patientId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
        return xRayReportRepository.findAllByAppointment_Patient_IdAndReleasedToPatientTrue(patientId);
    }
}
