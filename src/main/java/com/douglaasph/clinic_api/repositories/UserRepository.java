package com.douglaasph.clinic_api.repositories;

import com.douglaasph.clinic_api.models.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.employee IS NOT NULL
            AND (:name IS NULL OR :name = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (:position IS NULL OR u.employee.position = :position)
    """)
    Page<User> findEmployeesWithFilters(
            @Param("name") String name,
            @Param("position") Integer position,
            Pageable pageable
    );
}
