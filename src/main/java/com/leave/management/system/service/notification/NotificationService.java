package com.leave.management.system.service.notification;


import com.leave.management.system.dto.notification.NotificationDto;
import com.leave.management.system.model.Notification;


import java.util.List;


public interface NotificationService {
    List<Notification> getNotifications();
    void saveNotification(Notification notification);
    NotificationDto getUserLeaveNotificationByUserId();
    void markNotificationsAsRead(List<String> notificationIds);
}

