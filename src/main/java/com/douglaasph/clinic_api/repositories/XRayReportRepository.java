package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.XRayReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface XRayReportRepository extends JpaRepository<XRayReport, Long> {
}
