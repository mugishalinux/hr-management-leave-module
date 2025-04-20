package com.leave.management.system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeavePolicy extends BaseEntity {

    @Column(nullable = false)
    private double accrualRate;

    @Column(nullable = false)
    private double maxCarryForwardDays;
}