package com.pharmacy.MediNova.Controller;
import com.pharmacy.MediNova.Model.*;
import com.pharmacy.MediNova.Service.*;
import com.pharmacy.MediNova.utils.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private ContactUsService contactUsService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ShippingAddressService shippingAddressService;
    @Autowired
    private PurchaseRecordService purchaseRecordService;

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
    public String searchMedicine( @RequestParam("searchMedicine") String searchMedicine,
                                  @RequestParam(value = "category", required = false, defaultValue = "All") String category,
                                  Model model){
        List<Medicine> medicine;
        if(searchMedicine !=null && !searchMedicine.isEmpty()){
            medicine=medicineService.getMedicineByName(searchMedicine);
        }
        else{
            medicine=medicineService.getAllMedicine();
        }
        model.addAttribute("searchMedicines",medicine);
        model.addAttribute("keyword",searchMedicine);
        model.addAttribute("selectedCategory", category);
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

    //forget password logic code
    @GetMapping("/getForgetPassword")
    public String forgetPasswordPage() {
        return "ForgetPassword"; // Thymeleaf page name
    }

    @PostMapping("/processForgetPassword")
    public String processForgetPassword(@RequestParam("email") String email,
                                        HttpSession session,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        Customer customerByEmail = customerService.getCustomerByEmail(email);

        if (ObjectUtils.isEmpty(customerByEmail)) {
            redirectAttributes.addFlashAttribute("errMsg", "Invalid Email");
        } else {
            String resetToken = UUID.randomUUID().toString();
            customerService.updateCustomerResetToken(email, resetToken);

            // Generate URL
            String resetUrl = commonUtil.generateUrl(request) + "/resetPassword?token=" + resetToken;

            Boolean sendMail = commonUtil.sendMail(resetUrl, email);
            if (sendMail) {
                redirectAttributes.addFlashAttribute("succMsg", "Please check your email. Password reset link has been sent.");
            } else {
                redirectAttributes.addFlashAttribute("errMsg", "Something went wrong on the server. Mail not sent.");
            }
        }

        return "redirect:/getForgetPassword";
    }

    //resetPassword
    @GetMapping("/resetPassword")
    public String showResetPassword(@RequestParam String token,HttpSession session,Model model){
         Customer customerByToken = customerService.getCustomerByToken(token);
        if(customerByToken==null){
            model.addAttribute("errMsg", "Your link is invalid or expired");
            return "Message";
        }
        model.addAttribute("token", token);
        return "ResetPassword";
    }
    @PostMapping("/resetPasswordUpdate")
    public String showResetPassword(@RequestParam String token,
                                    @RequestParam String password,
                                    @RequestParam String confirmPassword,
                                    Model model){
        Customer customerByToken = customerService.getCustomerByToken(token);
        if(customerByToken==null){
            model.addAttribute("errMsg", "Your link is invalid or expired");
            return "Message";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errMsg", "Passwords do not match!");
            model.addAttribute("token", token); // send token back to re-render form
            return "ResetPassword"; // show same reset form again
        }

            customerByToken.setPassword(passwordEncoder.encode(password));
            customerByToken.setResetToken(null);
            customerService.updateCustomerPassword(customerByToken);
            model.addAttribute("succMsg","PasswordChange Sucessfully");
            return "redirect:/login";

    }

    @GetMapping("/addShippingAddress")
    public String addCustomerAddressForShipping(HttpSession session,Model model){
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");

        // Add customer object to model
        model.addAttribute("customer", customer);
        return "Customer/ShippingAddress";
    }

    @PostMapping("/saveAddress")
    public String addCustomerShippingAddress(@ModelAttribute ShippingDetails shippingDetails){
        shippingAddressService.addShippingAddress(shippingDetails);
        return "redirect:/showCheckout";
    }

    @GetMapping("/editShippingAddress/{id}")
    public String editAddressForm(@PathVariable Long id, Model model) {
        ShippingDetails address = shippingAddressService.findShippingAddressById(id);
        model.addAttribute("shippingAddress", address);
        return "Customer/EditSHippingAddress"; // Thymeleaf template
    }

    @PostMapping("/updateShippingAddress")
    public String updateShippingAddress(@ModelAttribute ShippingDetails address) {

        // Fetch the existing address by ID
        ShippingDetails existingAddress = shippingAddressService.findShippingAddressById(address.getId());
        if(existingAddress == null){
            // handle error: address not found
            return "redirect:/checkOut?error";
        }

        // Update editable fields only
        existingAddress.setCustomerId(existingAddress.getCustomerId());
        existingAddress.setRecipientName(address.getRecipientName());
        existingAddress.setPhoneNumber(address.getPhoneNumber());
        existingAddress.setAddress(address.getAddress());

        // Save updated entity
        shippingAddressService.updateShippingAddress(existingAddress);

        return "redirect:/showCheckout";
    }

    @GetMapping("/deleteShippingAddress/{id}")
    public String deleteMedicine(@PathVariable Long id){
       shippingAddressService.deleteShippingAddress(id);
        return "redirect:/showCheckout";
    }

    @GetMapping("/myOrder")
    public String getMyOrder(@AuthenticationPrincipal CustomCustomerDetails customerDetails,Model  model){
        Long customerId=customerDetails.getCustomerId();
        List<PurchaseRecord> myOrder=purchaseRecordService.getOrderById(customerId);
        model.addAttribute("myOrders",myOrder);
        return "Customer/MyOrder";
    }

    @GetMapping("/getMyOrderViewDetails/{id}")
    public String getMyOrderViewDetails(@PathVariable Long id, Model model) {
        // Fetch medicines for this purchase record
        List<PurchasedMedicine> medicines = purchaseRecordService.getMedicinesByPurchaseId(id);

        model.addAttribute("medicines", medicines);
        return "Customer/MyOrderViewMedicines";
    }

    @GetMapping("/backToMyOrder")
    public String backToMyOrder(){
        return "redirect:/myOrder";
    }

}


