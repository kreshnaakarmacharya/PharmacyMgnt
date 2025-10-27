package com.pharmacy.MediNova.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
}
