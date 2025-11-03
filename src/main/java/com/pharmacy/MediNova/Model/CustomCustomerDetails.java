package com.pharmacy.MediNova.Model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class CustomCustomerDetails implements UserDetails {
    private final Customer customerUser;

    public CustomCustomerDetails(Customer customerUser) {
        this.customerUser = customerUser;
    }

    // ✅ Authorities (roles/permissions)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+customerUser.getRole())); // e.g., ROLE_ADMIN
    }

    // ✅ Password
    @Override
    public String getPassword() {
        return customerUser.getPassword();
    }

    // ✅ Username
    @Override
    public String getUsername() {
        return customerUser.getEmail();
    }

    // ✅ Account status
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return customerUser.isEnabled();
    }

    // ✅ Custom getter (if you need full User object elsewhere)
    public Customer getUser() {
        return customerUser;
    }
}
