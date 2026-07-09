package com.douglaasph.clinic_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
     @Autowired
     private UserDetailsService userDetailsService;

     @Autowired
     private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(request -> request
                        // Public routes
                        .requestMatchers("/user/register/patient", "/user/login", "/refresh/{refreshToken}", "/user/register/patient/google", "/user/auth/google/check").permitAll()

                        // private routes that require authentication and role-based authorization
                        .requestMatchers(HttpMethod.GET, "/doctor/{id}").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/patient/{id}").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/appointment/{id}/cancel").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.PUT, "/appointment/{id}/diagnosis").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers("/user/register/doctor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/doctor").hasAnyRole("ADMIN", "PATIENT")
                        .requestMatchers(HttpMethod.GET, "/patient").hasAnyRole("ADMIN", "DOCTOR")
                        .requestMatchers(HttpMethod.POST, "/appointment").hasAnyRole("ADMIN", "PATIENT")

                        // private routes that require only authentication
                        .requestMatchers("/appointment").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
