package com.neura.loanmanagement.security;

import com.neura.loanmanagement.entity.AppUser;
import com.neura.loanmanagement.entity.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final UserRole role;
    private final Long customerId;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String password, UserRole role,
                         Long customerId, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
        this.enabled = enabled;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public static UserPrincipal create(AppUser user) {
        Long custId = user.getCustomer() != null ? user.getCustomer().getId() : null;
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                custId,
                user.isEnabled()
        );
    }

    public Long getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isLoanOfficer() {
        return role == UserRole.LOAN_OFFICER;
    }

    public boolean isCustomer() {
        return role == UserRole.CUSTOMER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

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
        return enabled;
    }
}
