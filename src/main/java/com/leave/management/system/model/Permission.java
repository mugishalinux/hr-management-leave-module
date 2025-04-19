package com.leave.management.system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;


@Entity
@Data
public class Permission {
    @Id
    private int id;
    private String name;
}
