package com.leave.management.system.model;




import com.leave.management.system.enums.NofiticationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {


    @Column(name="status",nullable = false)
    @Enumerated(EnumType.STRING)
    private NofiticationStatus status;


    @Column(name="description",nullable = false)
    private String description;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;




}
