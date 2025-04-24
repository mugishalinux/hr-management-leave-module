package com.leave.management.system.service.notification;
import com.leave.management.system.dto.notification.NotificationDto;
import com.leave.management.system.enums.NofiticationStatus;
import com.leave.management.system.model.Notification;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.NotificationRepository;
import com.leave.management.system.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;
    @Override
    public List<Notification> getNotifications() {
        return notificationRepository.findAll();
    }


    @Override
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }


    @Override
    public NotificationDto getUserLeaveNotificationByUserId() {
        User currentUser = securityUtils.getCurrentUser();
        List<Notification> allNotifications = notificationRepository.findByUserId(currentUser.getId());


        List<Notification> unread = allNotifications.stream()
                .filter(n -> n.getStatus() == NofiticationStatus.UNREAD)
                .toList();


        List<Notification> read = allNotifications.stream()
                .filter(n -> n.getStatus() == NofiticationStatus.READ)
                .toList();
        return new NotificationDto(unread, read);
    }


    @Override
    public void markNotificationsAsRead(List<String> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);


        if (notifications.isEmpty()) {
            throw new EntityNotFoundException("No notifications found for provided IDs.");
        }


        for (Notification notification : notifications) {
            notification.setStatus(NofiticationStatus.READ);
        }


        notificationRepository.saveAll(notifications);
    }
}

