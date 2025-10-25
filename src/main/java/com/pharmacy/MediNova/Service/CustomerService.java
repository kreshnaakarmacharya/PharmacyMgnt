package com.pharmacy.MediNova.Service;

import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
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
        Customer customer = null;
        if (email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            customer = customerRepository.findByEmail(email);
        } else {
            return null;
        }
        if (customer == null) {
            return null;
        }
        if (!customer.getPassword().equals(password)) {
            return null; // wrong password
        }
    return customer;
    }

    public void updateCustomerInformation(Customer c){
        customerRepository.save(c);
    }

    public Customer getCustomerById(Long id){
        return customerRepository.findById(id).get();
    }
}
