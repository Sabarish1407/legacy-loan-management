package com.neura.loanmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(options -> options.disable())) // For H2 console in testing
                .cors(withDefaults())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(handling -> handling.accessDeniedHandler(accessDeniedHandler))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(requests -> requests
                        // Public endpoints
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/metrics/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Customer creation and list: Admin & Loan Officer only
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasAnyRole("ADMIN", "LOAN_OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/customers").hasAnyRole("ADMIN", "LOAN_OFFICER")

                        // Customer detail / update / KYC
                        .requestMatchers(HttpMethod.PATCH, "/api/customers/*/kyc").hasAnyRole("ADMIN", "LOAN_OFFICER")
                        .requestMatchers(HttpMethod.GET, "/api/customers/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/customers/*").authenticated()

                        // Customer's loans lookup
                        .requestMatchers(HttpMethod.GET, "/api/customers/*/loans").authenticated()

                        // Loan Approval / Rejection: Loan Officer and Admin only
                        .requestMatchers(HttpMethod.PATCH, "/api/loans/applications/*/approve").hasAnyRole("ADMIN", "LOAN_OFFICER")
                        .requestMatchers(HttpMethod.PATCH, "/api/loans/applications/*/reject").hasAnyRole("ADMIN", "LOAN_OFFICER")

                        // Loan Disbursement: Loan Officer and Admin only
                        .requestMatchers(HttpMethod.POST, "/api/loans/*/disburse").hasAnyRole("ADMIN", "LOAN_OFFICER")

                        // Eligibility check: authenticated
                        .requestMatchers("/api/loans/eligibility").authenticated()

                        // Loan Application: authenticated
                        .requestMatchers(HttpMethod.POST, "/api/loans/applications").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/loans/applications/*").authenticated()

                        // Repayments and schedules: authenticated
                        .requestMatchers("/api/loans/*/repayments/**").authenticated()
                        .requestMatchers("/api/loans/*/schedule").authenticated()
                        .requestMatchers("/api/loans/*/balance").authenticated()

                        .anyRequest().authenticated());

        return http.build();
    }
}
