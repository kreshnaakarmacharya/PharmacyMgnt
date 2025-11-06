package com.pharmacy.MediNova.config;

import com.pharmacy.MediNova.Service.auth.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity (enable later if needed)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/images/**",
                                "/js/**",
                                "/css/**",
                                "/customerSignup",
                                "/login",
                                "/customerlogin",
                                "/customer/publicHomePage",
                                "/customer/customerHomePage",
                                "/medicine/details",
                                "/customer/home",
                                "/logout",
                                "/customer/search",
                                "/customer/contactUs",
                                "/customer/sendInquiry",
                                "/api/v1/auth/customerRegister",
                                "/customerVerification",
                                "/api/v1/auth/verify/**",
                                "/medicine/filter/{category}",
                                "/getForgetPassword",
                                "/processForgetPassword",
                                "/resetPassword/**",
                                "/resetPasswordUpdate"
                        ).permitAll()
                        .requestMatchers("/admin/**",
                                "/",
                                "/adminLogin",
                                "/pharmaAdmin",
                                "/adminDashboard",
                                "/medicineDashboard",
                                "/admin/inquiry",
                                "/medicine/addMedicine",
                                "/medicine/addMedicineForm",
                                "/view/{id}",
                                "/backToList",
                                "/updateMedicine",
                                "/deleteMedicine/{id}",
                                "/searchMedicine",
                                "/admin/customerDetails",
                                "/admin/active/{id}",
                                "/admin/inactive/{id}"
                        ).hasRole("ADMIN")
                        .requestMatchers("/customer/viewCart",
                                "/addToCart",
                                "/customer/increaseQuantity/{id}",
                                "/customer/decreaseQuantity/{id}",
                                "/customer/removeFromCart/{id}",
                                "/customer/clearCart",
                                "/customer/customerProfile",
                                "/customer/updateProfile",
                                "/customer/updateCustomerProfile"
                        ).hasAnyRole("ADMIN","CUSTOMER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
               .logout(logout -> logout
                    .logoutUrl("/logout")               // URL to trigger logout
                    .logoutSuccessUrl("/customer/publicHomePage")  // redirect URL after successful logout
                    .invalidateHttpSession(true)        // invalidates the HTTP session
                    .clearAuthentication(true)          // clear authentication
                    .permitAll()                        // allows all users to access the logout URL
               );
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

