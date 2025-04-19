package com.leave.management.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "department")

public class Department extends BaseEntity {
    @Id
    public String id = UUID.randomUUID().toString();
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private String description;
}
