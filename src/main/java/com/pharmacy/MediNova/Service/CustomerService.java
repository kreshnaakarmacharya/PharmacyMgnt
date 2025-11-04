package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.CustomCustomerDetails;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import com.pharmacy.MediNova.request.RegisterRequest;
import com.pharmacy.MediNova.response.RegisterResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Random;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public RegisterResponse register(RegisterRequest registerRequest,String confirmPassword){
        LocalDate dob = registerRequest.getDateOfBirth();
        if(dob == null){
            throw new IllegalArgumentException("Date of birth is null!");
        }

        int age = Period.between(dob, LocalDate.now()).getYears();
        if(age < 18){
            throw new IllegalArgumentException("You must be at least 18 years old!");
        }

        String password = registerRequest.getPassword();
        if(password.length() < 8){
            throw new IllegalArgumentException("Password length should be 8 characters");
        }

        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#%&+!]).{8,}$";
        if(!password.matches(pattern)){
            throw new IllegalArgumentException("Password must contain one upper case letter, one lower case letter, one digit and one special character");
        }

        if(!password.equals(confirmPassword)){
            throw new IllegalArgumentException("Passwords do not match");
        }

        Customer existingCustomer = customerRepository.findByEmail(registerRequest.getEmail());

        if (existingCustomer != null) {
            if (existingCustomer.isVerified()) {
                // Already verified → cannot resend OTP
                throw new RuntimeException("Customer is already verified!");
            } else {
                // Unverified → generate new OTP and resend
                String otp = generateOtp();
                existingCustomer.setOtp(otp);
                customerRepository.save(existingCustomer);
                sendEmailVerification(existingCustomer.getEmail(), otp);

                return RegisterResponse.builder()
                        .fullName(existingCustomer.getFullName())
                        .email(existingCustomer.getEmail())
                        .phoneNumber(existingCustomer.getPhoneNumber())
                        .address(existingCustomer.getAddress())
                        .dateOfBirth(existingCustomer.getDateOfBirth())
                        .gender(existingCustomer.getGender())
                        .build();
            }
        }

        // New user → create and save
        Customer c = Customer.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .address(registerRequest.getAddress())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .gender(registerRequest.getGender())
                .role("CUSTOMER")
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        String otp = generateOtp();
        c.setOtp(otp);
        Customer savedCustomer = customerRepository.save(c);
        sendEmailVerification(c.getEmail(), otp);

        return RegisterResponse.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .address(registerRequest.getAddress())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .gender(registerRequest.getGender())
                .build();
    }

    public Customer login(String email, String password, HttpSession session) {
        try{
            UsernamePasswordAuthenticationToken authReq =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication auth = authenticationManager.authenticate(authReq);

            // ✅ Save authentication in SecurityContext
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(auth);

            // ✅ Store SecurityContext in session
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );
            CustomCustomerDetails customCustomerDetails =  (CustomCustomerDetails) auth.getPrincipal();
            return customCustomerDetails.getUser();
        }
        catch (AuthenticationException e){
            return null;
        }

    }


    public void updateCustomerInformation(Customer c){
        customerRepository.save(c);
    }

    public Customer getCustomerById(Long id){
        return customerRepository.findById(id).get();
    }



    private String generateOtp(){
        Random random= new Random();
        int otpValue=100000+random.nextInt(900000);
        return String.valueOf(otpValue);
    }

    private void sendEmailVerification(String email,String otp){
        String subject = "Email Verification";
        String body = "Your Verification OTP is"+otp;
        emailService.sendEmail(email,subject,body);
    }

    public void verify(String email,String otp){
        Customer customer = customerRepository.findByEmail(email);
        if(customer == null){
            throw new RuntimeException("User not found!");
        }
        else if(customer.isVerified()){
            throw new RuntimeException("User is already verified!");
        }
        else if(otp.equals(customer.getOtp())){
            customer.setVerified(true);
            customerRepository.save(customer);
        }
        else{
            throw new RuntimeException("Internal Server Error!");
        }
    }

    public List<Customer> getAllCustomer(){
        return customerRepository.findByRole("CUSTOMER");
    }
    public void setEnable(Long id, boolean status) {
        // Find customer by ID
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        // Set enabled status
        customer.setEnabled(status);

        // Save the updated customer
        customerRepository.save(customer);
    }


}
