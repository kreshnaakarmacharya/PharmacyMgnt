package com.pharmacy.MediNova.Service.auth;

import com.pharmacy.MediNova.Model.CustomCustomerDetails;
import com.pharmacy.MediNova.Model.Customer;
import com.pharmacy.MediNova.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer user = customerRepository.findByEmail(username);

        if(user == null){
            throw new UsernameNotFoundException("Username not found");
        }
        return new CustomCustomerDetails(user);
    }
}
