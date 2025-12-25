package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.PurchaseRecord;
import com.pharmacy.MediNova.Service.PurchaseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @GetMapping("/nonPrescribeMedicineOrderDetails")
    public String nonPrescribedMedicineOrderDetails(Model model) {
        purchaseRecordService.markOrdersAsSeenByAdmin();
        List<PurchaseRecord> allRecords = purchaseRecordService.findAllLatestFirst();

        // Map purchase record id -> customer name
        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord record : allRecords) {
            String name = purchaseRecordService.getCustomerNameById(record.getCustomerId());
            customerNames.put(record.getId(), name);
        }

        model.addAttribute("purchaseRecord", allRecords);
        model.addAttribute("customerNames", customerNames);
        model.addAttribute("todaySales", purchaseRecordService.getTodaySales());
        return "Admin/NonPrescribedOrder";
    }
}
