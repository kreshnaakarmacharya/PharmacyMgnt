package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.Admin;
import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private MedicineService medicineService;
    @Autowired
    private ContactUsService  contactUsService;
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

}
