package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Service.CustomerService;
import com.pharmacy.MediNova.request.RegisterRequest;
import com.pharmacy.MediNova.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerService customerService;

    @PostMapping("/customerRegister")
    public String userRegisterSubmission(@ModelAttribute RegisterRequest registerRequest,
                                         @RequestParam("confirmPassword") String confirmPassword,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Use unified register method
            RegisterResponse response = customerService.register(registerRequest, confirmPassword);

            // Redirect to verification page with email
            return "redirect:/customerVerification?email=" + response.getEmail();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("email", registerRequest.getEmail());
            return "redirect:/register"; // back to registration page
        }
    }


    @PostMapping("/verify")
    public String verifiedUser(@RequestParam String email,
                               @RequestParam String otp,
                               RedirectAttributes redirectAttributes) {
        try {
            customerService.verify(email, otp);
            // Add flash message for success
            redirectAttributes.addFlashAttribute("successMessage",
                    "User verified successfully! Please login now.");
            // Redirect to login page
            return "redirect:/login";
        } catch (RuntimeException e) {
            // Add flash message for error
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Redirect back to verification page with email
            return "redirect:/customerVerification?email=" + email;
        }
    }

}
