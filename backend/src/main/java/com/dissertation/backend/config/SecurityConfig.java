package com.dissertation.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final SecurityErrorHandlers securityErrorHandlers;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, SecurityErrorHandlers securityErrorHandlers) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.securityErrorHandlers = securityErrorHandlers;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(securityErrorHandlers.entryPoint())
                        .accessDeniedHandler(securityErrorHandlers.accessDeniedHandler()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/modules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/enrolments/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/assessments/**").hasRole("LECTURER")
                        .requestMatchers(HttpMethod.PATCH, "/api/assessments/**").hasRole("LECTURER")
                        .requestMatchers(HttpMethod.DELETE, "/api/assessments/**").hasRole("LECTURER")

                        .requestMatchers(HttpMethod.GET, "/api/assessments/*/feedback").hasRole("LECTURER")
                        .requestMatchers(HttpMethod.GET, "/api/assessments/*/students/*/feedback").hasRole("LECTURER")

                        .requestMatchers("/api/phrases/**").hasRole("LECTURER")


                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}