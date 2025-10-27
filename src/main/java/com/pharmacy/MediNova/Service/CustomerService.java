package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import com.pharmacy.MediNova.request.RegisterRequest;
import com.pharmacy.MediNova.response.RegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Random;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;
    public void addUser(Customer c, String confirmPassword){
        LocalDate dob = c.getDateOfBirth();
        if(dob == null){
            throw new IllegalArgumentException("Date of birth is null!");
        }

        int age = Period.between(dob, LocalDate.now()).getYears();
        if(age < 18){
            throw new IllegalArgumentException("You must be at least 18 years old!");
        }

        String password = c.getPassword();
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

        customerRepository.save(c);
    }
    public Customer login(String email, String password) {
        Customer byEmail = customerRepository.findByEmail(email);

        if (byEmail == null) {
            // User not found
            return null;
        }

        if (!byEmail.isVerified()) {
            // User exists but not verified
            return null;
        }

        if (!byEmail.getPassword().equals(password)) {
            // Wrong password
            return null;
        }

        // All good
        return byEmail;
    }


    public void updateCustomerInformation(Customer c){
        customerRepository.save(c);
    }

    public Customer getCustomerById(Long id){
        return customerRepository.findById(id).get();
    }

    public RegisterResponse register(RegisterRequest registerRequest){
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
                .password(registerRequest.getPassword())
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


}
