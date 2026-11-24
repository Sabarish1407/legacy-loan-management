package com.neura.loanmanagement.integration.payment;

import com.neura.loanmanagement.entity.enums.PaymentMethod;
import com.neura.loanmanagement.entity.enums.RepaymentStatus;
import com.neura.loanmanagement.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedPaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(SimulatedPaymentGateway.class);

    private final String defaultMode;

    public SimulatedPaymentGateway(@Value("${loan.payment-gateway.simulation-mode:SUCCESS}") String defaultMode) {
        this.defaultMode = defaultMode;
    }

    public PaymentResult processPayment(String idempotencyKey, BigDecimal amount, PaymentMethod method, String notes) {
        log.info("Processing simulated payment for idempotencyKey={}, amount={}, method={}",
                idempotencyKey, amount, method);

        if (notes != null && notes.equalsIgnoreCase("SIMULATE_GATEWAY_FAILURE")) {
            log.warn("Payment simulation forced failure via note: {}", idempotencyKey);
            return new PaymentResult(false, null, "Payment gateway rejected the transaction", RepaymentStatus.FAILED);
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero");
        }

        String gatewayRef = "PG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("Simulated payment SUCCESS, gatewayRef={}", gatewayRef);
        return new PaymentResult(true, gatewayRef, "Payment processed successfully", RepaymentStatus.SUCCESS);
    }

    public static class PaymentResult {
        private final boolean success;
        private final String gatewayReference;
        private final String message;
        private final RepaymentStatus status;

        public PaymentResult(boolean success, String gatewayReference, String message, RepaymentStatus status) {
            this.success = success;
            this.gatewayReference = gatewayReference;
            this.message = message;
            this.status = status;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getGatewayReference() {
            return gatewayReference;
        }

        public String getMessage() {
            return message;
        }

        public RepaymentStatus getStatus() {
            return status;
        }
    }
}
