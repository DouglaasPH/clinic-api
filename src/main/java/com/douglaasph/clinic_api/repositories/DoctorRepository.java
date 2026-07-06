package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.Doctor;
import com.douglaasph.clinic_api.models.entities.enums.Specialties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("""
        SELECT d FROM Doctor d 
        WHERE (:name IS NULL OR :name = '' OR LOWER(d.user.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:specialty IS NULL OR d.specialty = :specialty)
    """)
    List<Doctor> findByOptionalFilters(
            @Param("name") String name,
            @Param("specialty") Integer specialty
    );
}
