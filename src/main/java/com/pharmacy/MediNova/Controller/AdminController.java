package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ContactUsService  contactUsService;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private PurchaseRecordService purchaseRecordService;

    @GetMapping("/pharmaAdmin")
    public String getPharmaAdmin(Model model) {
        try{
            Map<String, Object> result = this.adminService.getMonthlySalesCount();
            model.addAttribute("months", result.get("months"));
            model.addAttribute("values", result.get("records"));
            model.addAttribute("totalCustomers",customerService.getTotalCustomerCount());
            model.addAttribute("totalOrders",purchaseRecordService.getTotalOrderCount());
            model.addAttribute("prescribedOrder",purchaseRecordService.countOrderByRequiredPrescriptionTrue());
            model.addAttribute("totalMedicine",medicineService.countTotalMedicine());
        } catch (Exception er){
            System.out.println("### Error occurred while trying to pull monthly sales record ###");
        }

        return "Admin/AdminHomePage";
    }

    @GetMapping("/adminDashboard")
    public String getAdminDashboard(){
        return "redirect:/pharmaAdmin";
    }

    @GetMapping("/medicineDashboard")
    public String medicineDashboard(Model model) {
        List<Medicine> allMedicine = medicineService.getAllMedicineAlphabetically();
        model.addAttribute("medicine", allMedicine);
        return "Admin/MedicineHomePage";
    }


    @GetMapping("/admin/inquiry")
    public String getInquiry(Model model){
        model.addAttribute("inquiry",contactUsService.getContactUs());
        return "Admin/Inquiry";
    }

    @GetMapping("/admin/customerDetails")
    public String getCustomerDetails(Model model){
        List<Customer> allCustomer = customerService.getAllCustomer();
        model.addAttribute("customer", allCustomer);
        return "Admin/CustomerDetails";
    }

    @GetMapping("/viewSalesStatement")
    public String salesStatementView( Model model){
        List<PurchaseRecord> allRecords = purchaseRecordService.getAllPurchaseRecords();

        // Map purchase record id -> customer name
        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord record : allRecords) {
            String name = purchaseRecordService.getCustomerNameById(record.getCustomerId());
            customerNames.put(record.getId(), name);
        }

        model.addAttribute("purchaseRecord", allRecords);
        model.addAttribute("customerNames", customerNames);
        model.addAttribute("todaySales", purchaseRecordService.getTodaySales());
        model.addAttribute("totalOrders",purchaseRecordService.getTotalOrderCount());
        model.addAttribute("prescribedOrder",purchaseRecordService.countOrderByRequiredPrescriptionTrue());
        return "Admin/SalesStatement";
    }

    @GetMapping("/viewCustomerPurchasedMedicine/{id}")
    public String viewCustomerPurchasedMedicine(@PathVariable Long id, Model model) {
        // Fetch medicines for this purchase record
        List<PurchasedMedicine> medicines = purchaseRecordService.getMedicinesByPurchaseId(id);

        model.addAttribute("medicines", medicines);
        return "Admin/ViewCustomerPurchasedMedicine";
    }


    @GetMapping("/sales")
    public String viewSales(
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {

        LocalDate today = LocalDate.now();

        if (fromDate.isAfter(today)) {
            model.addAttribute("error", "From date cannot be after today");
            return "redirect:/sales";
        }

        if (toDate.isBefore(today)) {
            model.addAttribute("error", "To date cannot be before today");
            return "redirect:/sales";
        }

        if (fromDate.isAfter(toDate)) {
            model.addAttribute("error", "From date cannot be after To date");
            return "redirect:/sales";
        }

        List<PurchaseRecord> records =
                purchaseRecordService.getSales(fromDate, toDate);

        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord pr : records) {
            customerNames.put(pr.getId(),
                    purchaseRecordService.getCustomerNameById(pr.getCustomerId()));
        }

        model.addAttribute("purchaseRecord", records);
        model.addAttribute("customerNames", customerNames);
        model.addAttribute("todaySales",
                purchaseRecordService.getTotalSales(fromDate, toDate));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
//        model.addAttribute("totalOrders", records.size());

        return "Admin/SalesStatement";
    }

    @GetMapping("/orderDetails")
    public String orderDetails() {
        return "Admin/Order";
    }

    @PostMapping("/updateOrderStatus")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String action) {

       if ("dispatch".equals(action)) {
            purchaseRecordService.markDispatched(orderId);
        } else if ("deliver".equals(action)) {
            purchaseRecordService.markDelivered(orderId);
        }
        return "redirect:/nonPrescribeMedicineOrderDetails";
    }

    @PostMapping("/updatePrescribedOrderStatus")
    public String updatePrescribedOrderStatus(@RequestParam Long orderId,
                                              @RequestParam String action) {
        if("approved".equals(action)) {
            purchaseRecordService.markApproved(orderId);
        } else if ("rejected".equals(action)) {
            purchaseRecordService.markRejected(orderId);
        } else if ("dispatch".equals(action)) {
            purchaseRecordService.markDispatched(orderId);
        } else if ("deliver".equals(action)) {
            purchaseRecordService.markDelivered(orderId);
        }
        return "redirect:/prescribedMedicineOrderDetails";
    }

    @GetMapping("/admin/new-order-count")
    @ResponseBody
    public long getNewOrderCount() {
        return purchaseRecordService.countUnseenOrders();
    }

    @GetMapping("/viewDetailOrderedMedicine/{id}")
    public String viewDetailOrderedMedicine(@PathVariable Long id, Model model) {
        // Fetch medicines for this purchase record
        List<PurchasedMedicine> medicines = purchaseRecordService.getMedicinesByPurchaseId(id);

        model.addAttribute("medicines", medicines);
        return "Admin/viewDetailOrderedMedicine";
    }

    @GetMapping("/backToOrderedMedicine")
    public String backToOrderedMedicine(){
        return "redirect:/orderDetails";
    }
}
