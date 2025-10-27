package com.pharmacy.MediNova.request;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegisterRequest {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
    private String password;
}
