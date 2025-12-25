package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.CustomCustomerDetails;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Service.NotificationSevice;
import com.pharmacy.MediNova.Service.PurchaseRecordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/notifications")
public class NotificationController {

    @Autowired
    private NotificationSevice notificationService;
    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @GetMapping
    public String notifications(@AuthenticationPrincipal CustomCustomerDetails customerDetails, Model model) {
        model.addAttribute("notifications",
                notificationService.getCustomerNotifications(customerDetails.getCustomerId()));
        return "Customer/Notification";
    }

    @GetMapping("/count")
    @ResponseBody
    public long unreadCount(@AuthenticationPrincipal CustomCustomerDetails customerDetails) {
        return notificationService.getUnreadCount(customerDetails.getCustomerId());
    }

    @PostMapping("/read/{id}")
    @ResponseBody
    public void markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }


}

