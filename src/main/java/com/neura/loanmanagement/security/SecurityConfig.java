package com.neura.loanmanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
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
            .csrf().disable()
            .headers().frameOptions().disable().and() // For H2 console in testing
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .httpBasic().authenticationEntryPoint(authenticationEntryPoint).and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
            .authenticationProvider(authenticationProvider())
            .authorizeRequests()
                // Public endpoints
                .antMatchers("/actuator/health", "/actuator/info", "/actuator/metrics/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()

                // Customer creation and list: Admin & Loan Officer only
                .antMatchers(HttpMethod.POST, "/api/customers").hasAnyRole("ADMIN", "LOAN_OFFICER")
                .antMatchers(HttpMethod.GET, "/api/customers").hasAnyRole("ADMIN", "LOAN_OFFICER")

                // Customer detail / update / KYC
                .antMatchers(HttpMethod.PATCH, "/api/customers/*/kyc").hasAnyRole("ADMIN", "LOAN_OFFICER")
                .antMatchers(HttpMethod.GET, "/api/customers/*").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/customers/*").authenticated()

                // Customer's loans lookup
                .antMatchers(HttpMethod.GET, "/api/customers/*/loans").authenticated()

                // Loan Approval / Rejection: Loan Officer and Admin only
                .antMatchers(HttpMethod.PATCH, "/api/loans/applications/*/approve").hasAnyRole("ADMIN", "LOAN_OFFICER")
                .antMatchers(HttpMethod.PATCH, "/api/loans/applications/*/reject").hasAnyRole("ADMIN", "LOAN_OFFICER")

                // Loan Disbursement: Loan Officer and Admin only
                .antMatchers(HttpMethod.POST, "/api/loans/*/disburse").hasAnyRole("ADMIN", "LOAN_OFFICER")

                // Eligibility check: authenticated
                .antMatchers("/api/loans/eligibility").authenticated()

                // Loan Application: authenticated
                .antMatchers(HttpMethod.POST, "/api/loans/applications").authenticated()
                .antMatchers(HttpMethod.GET, "/api/loans/applications/*").authenticated()

                // Repayments and schedules: authenticated
                .antMatchers("/api/loans/*/repayments/**").authenticated()
                .antMatchers("/api/loans/*/schedule").authenticated()
                .antMatchers("/api/loans/*/balance").authenticated()

                .anyRequest().authenticated();

        return http.build();
    }
}
