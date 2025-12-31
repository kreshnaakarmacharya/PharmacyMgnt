package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Notification;
import com.pharmacy.MediNova.Repository.NotificationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class NotificationSevice {
    @Autowired
    private NotificationRepo notificationRepo;

    public void send(Long customerId, Long orderId, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setOrderId(orderId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepo.save(notification);
    }

    public List<Notification> getCustomerNotifications(Long customerId) {
        return notificationRepo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }


}
