package com.pharmacy.MediNova.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                                "/customerVerification",
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
                                "/customerRegister",
                                "/verify"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")      // custom login page
                        .loginProcessingUrl("/doLogin") // where login form submits
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                );
//                .formLogin(Customizer.withDefaults())
//                .logout(Customizer.withDefaults());
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout=true")
//                        .permitAll()
//                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

