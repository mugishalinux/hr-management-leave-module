package com.leave.management.system.model;

import com.leave.management.system.enums.LeaveTypeStatus;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "leave_type", uniqueConstraints = {
        @UniqueConstraint(name = "UK_leave_type_name", columnNames = "name")
})
public class LeaveType extends BaseEntity {

    @Column(name = "name", length = 90, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "requires_attachment", nullable = false)
    private boolean leaveTypeRequiresAttachment;

    @Column(name = "requires_reason", nullable = false)
    private boolean isLeaveTypeRequireReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)

    private LeaveTypeStatus status = LeaveTypeStatus.ACTIVE;

    @Column(name = "affects_balance", nullable = false)
    private boolean affectsBalance;
}
