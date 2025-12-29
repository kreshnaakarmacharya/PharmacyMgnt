package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Service.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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

    @GetMapping("/esewa/success")
    public String esewaSuccess(@PathVariable("data") String data){
        return null;
    }

    @GetMapping("/esewa/fail")
    public String esewaFail(@PathVariable("data") String data){
        return null;
    }

    @GetMapping("/signature")
    public String signature(Model model) {
        try {
            int totalAmt = 110;
            int tranId = ThreadLocalRandom.current().nextInt(1, 10_000_000);
            String productCode = "EPAYTEST";

            String totalAmount = "total_amount="+totalAmt;
            String transactionUuid = "transaction_uuid="+tranId;
            String productCode2 = "product_code=EPAYTEST";

            String message = String.join(",",
                    totalAmount,
                    transactionUuid,
                    productCode2
            );

            String secret = "8gBm/:&EnhH.1/q";
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(),"HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));

            model.addAttribute("hash", hash);
            model.addAttribute("tranId", tranId);
            model.addAttribute("totalAmt", totalAmt);
            model.addAttribute("productCode", productCode);
            System.out.println(hash);
            return "esewa";
        }
        catch (Exception e){
            System.out.println("Error");
            return null;
        }
    }
}
