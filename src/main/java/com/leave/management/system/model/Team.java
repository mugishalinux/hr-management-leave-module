package com.leave.management.system.model;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne
    @JoinColumn(name = "lead_id")
    private User lead;
}
