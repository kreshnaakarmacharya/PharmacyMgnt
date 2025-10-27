package com.pharmacy.MediNova.Controller;

import com.pharmacy.MediNova.Service.CustomerService;
import com.pharmacy.MediNova.request.RegisterRequest;
import com.pharmacy.MediNova.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
       RegisterResponse registerResponse= customerService.register(registerRequest);
       return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifiedUser(@RequestParam String email,@RequestParam String otp){
        try{
            customerService.verify(email,otp);
            return new ResponseEntity<>("User Verified Successfully", HttpStatus.OK);
        }
        catch (RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
