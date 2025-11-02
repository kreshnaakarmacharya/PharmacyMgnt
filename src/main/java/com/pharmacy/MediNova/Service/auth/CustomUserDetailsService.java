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
//
//    public CustomUserDetailsService(UserRepository userRepo) {
//        this.userRepo = userRepo;
//    }

//    @Autowired
//    private PasswordEncoder passwordEncoder;

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//////        User user = userRepo.findByUsername(username)
//////                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
////        return org.springframework.security.core.userdetails.User
////                .withUsername("krina")
////                .password(passwordEncoder.encode("test123"))
////                .roles("CLIENT")
////                .build();
//    }


//    public CustomUserDetailsService(UserRepository userRepo) {
//        this.userRepo = userRepo;
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer user = customerRepository.findByEmail(username);

        if(user == null){
            throw new UsernameNotFoundException("Username not found");
        }
        return new CustomCustomerDetails(user);
    }
}
