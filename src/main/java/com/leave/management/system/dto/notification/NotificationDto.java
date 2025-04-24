package com.leave.management.system.dto.notification;


import com.leave.management.system.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public  class NotificationDto {
    private List<Notification> unreadNotifications;
    private List<Notification> readNotifications;
}

