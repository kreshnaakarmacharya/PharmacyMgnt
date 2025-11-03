package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.Admin;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
