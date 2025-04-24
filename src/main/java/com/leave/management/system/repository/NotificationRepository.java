package com.leave.management.system.repository;
import com.leave.management.system.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    @Query("SELECT n FROM Notification n WHERE n.leaveApplication.user.id = :userId")
    List<Notification> findByUserId(@Param("userId") String userId);
}



