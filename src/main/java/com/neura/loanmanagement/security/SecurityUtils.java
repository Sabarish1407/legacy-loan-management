package com.neura.loanmanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) auth.getPrincipal());
        }
        return Optional.empty();
    }

    public static String getCurrentUsername() {
        return getCurrentUser()
                .map(UserPrincipal::getUsername)
                .orElse("anonymousUser");
    }

    public static boolean isCurrentUserCustomer() {
        return getCurrentUser()
                .map(UserPrincipal::isCustomer)
                .orElse(false);
    }

    public static boolean isCurrentUserAdminOrOfficer() {
        return getCurrentUser()
                .map(u -> u.isAdmin() || u.isLoanOfficer())
                .orElse(false);
    }

    public static Optional<Long> getCurrentCustomerId() {
        return getCurrentUser()
                .map(UserPrincipal::getCustomerId);
    }
}
