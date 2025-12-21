package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String getPharmaAdmin(){
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
    @GetMapping("/admin/active/{id}")
    public String activateCustomer(@PathVariable Long id) {
        customerService.setEnable(id, true);
        return "redirect:/admin/customerDetails"; // go back to list
    }

    // Deactivate customer
    @GetMapping("/admin/inactive/{id}")
    public String deactivateCustomer(@PathVariable Long id) {
        customerService.setEnable(id, false);
        return "redirect:/admin/customerDetails"; // go back to list
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
    public String orderDetails(Model model) {
        List<PurchaseRecord> allRecords = purchaseRecordService.findAll();

        // Map purchase record id -> customer name
        Map<Long, String> customerNames = new HashMap<>();
        for (PurchaseRecord record : allRecords) {
            String name = purchaseRecordService.getCustomerNameById(record.getCustomerId());
            customerNames.put(record.getId(), name);
        }

        model.addAttribute("purchaseRecord", allRecords);
        model.addAttribute("customerNames", customerNames);
        model.addAttribute("todaySales", purchaseRecordService.getTodaySales());
        return "Admin/Order";
    }

    @PostMapping("/productDelivered")
    @ResponseBody
    public ResponseEntity<?> markAsDelivered(@RequestParam Long orderId) {

        purchaseRecordService.markDelivered(orderId);
        return ResponseEntity.ok().build();
    }


}
