package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Model.ContactUs;
import com.pharmacy.MediNova.Model.CustomCustomerDetails;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Model.Medicine;
import com.pharmacy.MediNova.Service.ContactUsService;
import com.pharmacy.MediNova.Service.CustomerService;
import com.pharmacy.MediNova.Service.MedicineService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ContactUsService contactUsService;

    @GetMapping("/customerSignup")
    public String getUserRegistration(){
        return "Customer/CustomerSignup";
    }

    @GetMapping("/customerVerification")
    public String getCustomerVerification(@RequestParam("email") String email,Model model){
        model.addAttribute("email", email);
        return "Customer/CustomerVerification";
    }

    @GetMapping("/login")
    public String getLogin(){

        return "Customer/CustomerLogin";
    }
    @PostMapping("/customerlogin")
    public String loginSubmit(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model, HttpSession session
    ){
        Customer customer=customerService.login(email, password, session);
        if (customer == null) {
            model.addAttribute("errorMessage", "Invalid login id or password");
            return "Customer/CustomerLogin";
        }
        if(!customer.isVerified()){
            model.addAttribute("errorMessage", "Your account is not verified yet. Please verify your email first.");
           return "Customer/CustomerLogin";
        }
       session.setAttribute("loggedInCustomer", customer);
        if(customer.getRole().equals("ADMIN")){
            return "redirect:/pharmaAdmin";
        }
        else if(customer.getRole().equals("CUSTOMER")){
            return "redirect:/customer/customerHomePage";
        }
        else{
            return "redirect:/customer/publicHomePage";
        }
    }
    @GetMapping("/customer/publicHomePage")
    public String getHome(Model model){
        model.addAttribute("medList", medicineService.getAllMedicine());
        return "Customer/PublicHomePage";
    }

    @GetMapping("/customer/customerHomePage")
    public String customerHomePage (Model model,HttpSession session){
        // Check if user is logged in
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            // Not logged in → send back to public page
            return "redirect:/customer/publicHomePage";
        }
        model.addAttribute("medList", medicineService.getAllMedicine());
        return "Customer/CustomerHomePage";
    }

    @GetMapping("/medicine/details")
    public String getDetailOfMedicine( @RequestParam("id") long id, Model model){
        Medicine medicine=medicineService.findMedicineById(id);
        model.addAttribute("medicine", medicine);
        return "Customer/MedicineDetails";
    }

    @GetMapping("/customer/home")
    public String getHome(){
        return "redirect:/customer/customerHomePage";
    }

    @GetMapping("/logout")
    public String Logout(HttpSession session){
        session.invalidate();
        return "redirect:/customer/publicHomePage";
    }

    @GetMapping("/customer/search")
    public String searchMedicine( @RequestParam("searchMedicine") String searchMedicine, Model model){
        List<Medicine> medicine;
        if(searchMedicine !=null && !searchMedicine.isEmpty()){
            medicine=medicineService.getMedicineByName(searchMedicine);
        }
        else{
            medicine=medicineService.getAllMedicine();
        }
        model.addAttribute("searchMedicines",medicine);
        model.addAttribute("keyword",searchMedicine);
        return "Customer/SearchMedicine";
    }

//    This is contact us controller getMapping
    @GetMapping("/customer/contactUs")
    public String contactUs(){
        return "Customer/ContactUs";
    }
    //This is contactUs PostMapping

    @PostMapping("/customer/sendInquiry")
    @ResponseBody
    public Map<String,String> contactUs(@ModelAttribute ContactUs contactUs){
        contactUsService.addContactUs(contactUs);
        Map<String,String> response=new HashMap<>();
        response.put("status","success");
        response.put("message","Your message was send Sucessfully!");
        return response;
    }

    @GetMapping("/customer/customerProfile")
    public String getProfile(Model model,HttpSession session){
        Customer sessioncustomer=(Customer) session.getAttribute("loggedInCustomer");
        if(sessioncustomer==null){
            return "redirect:/customer/publicHomePage";
        }
        Customer customer=(Customer) customerService.getCustomerById(sessioncustomer.getId());
        model.addAttribute("customer",customer);
        return "Customer/CustomerProfile";
    }

    @GetMapping("/customer/updateProfile")
    public String editProfile(Model model,HttpSession session){
        Customer customer=(Customer) session.getAttribute("loggedInCustomer");
        if(customer==null){
            return "redirect:/customer/publicHomePage";
        }
        Customer c=(Customer) customerService.getCustomerById(customer.getId());
        model.addAttribute("customer",c);
        return "Customer/EditProfile";
    }

    @PostMapping("/customer/updateCustomerProfile")
    public String updateCustomerProfile( @ModelAttribute Customer updateCustomer){
        Customer c=customerService.getCustomerById(updateCustomer.getId());
        c.setFullName(updateCustomer.getFullName());
        c.setEmail(updateCustomer.getEmail());
        c.setPhoneNumber(updateCustomer.getPhoneNumber());
        c.setAddress(updateCustomer.getAddress());
        c.setDateOfBirth(updateCustomer.getDateOfBirth());
        c.setGender(updateCustomer.getGender());
        customerService.updateCustomerInformation(c);
        return "redirect:/customer/customerProfile";
    }

    @GetMapping("/customer/order")
    public String OrderPage(){
        return "Customer/Order";
    }


}


