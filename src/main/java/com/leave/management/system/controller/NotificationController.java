package com.leave.management.system.controller;
import com.leave.management.system.dto.notification.NotificationDto;
import com.leave.management.system.model.Notification;
import com.leave.management.system.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@CrossOrigin("*")
@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final   NotificationService notificationService;

    @GetMapping("/leave/user")
    public NotificationDto createNotificationByLeaveId(){
        return notificationService.getUserLeaveNotificationByUserId();
    }

    @PutMapping("/mark-read")
    public ResponseEntity<String> markNotificationAsRead(@RequestBody List<String> notificationIds) {
        notificationService.markNotificationsAsRead(notificationIds);
        return ResponseEntity.ok("Notification marked as READ.");
    }
}

