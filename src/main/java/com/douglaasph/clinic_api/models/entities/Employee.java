package com.douglaasph.clinic_api.models.entities;

import com.douglaasph.clinic_api.models.entities.enums.Position;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CRM or CRTR
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @Column(nullable = false)
    private Integer position;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Employee(Long id, String licenseNumber, Position position, User user) {
        this.id = id;
        this.licenseNumber = licenseNumber;
        setPosition(position);
        this.user = user;
    }

    public Position getPosition() {
        return Position.valueOf(position);
    }

    public void setPosition(Position position) {
        if (position != null) {
            this.position = position.getCode();
        }
    }
}
