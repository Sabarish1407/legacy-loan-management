package com.neura.loanmanagement.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.entity.enums.PaymentMethod;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("E2E Scenario 1: Complete Successful Loan Lifecycle")
class Scenario1SuccessfulLoanLifecycleE2ETest extends AbstractE2ESupport {

    @Test
    @DisplayName("Complete Lifecycle: Create Customer -> Submit App -> Eligibility -> Approve -> Disburse -> Schedule -> Repay -> Close")
    void testCompleteLoanLifecycle() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // Step 1: Create customer
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                "Taylor", "Morgan", "taylor." + uid + "@example.com", "+1-555-8001",
                "NAT-E2E1-" + uid, "500 Financial Way", LocalDate.of(1987, 8, 14),
                new BigDecimal("80000.00"), EmploymentType.SALARIED, 760);

        Response custResult = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(customerRequest))
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().response();

        JsonNode custNode = objectMapper.readTree(custResult.asString());
        long customerId = custNode.get("data").get("id").asLong();
        assertTrue(customerId > 0);

        // Step 2: Submit loan application
        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("60000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("80000.00"));

        Response appResult = givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(appRequest))
                .when().post("/api/loans/applications")
                .then().statusCode(201)
                .body("data.status", equalTo("SUBMITTED"))
                .extract().response();

        JsonNode appNode = objectMapper.readTree(appResult.asString());
        long applicationId = appNode.get("data").get("id").asLong();

        // Step 3: Eligibility assessment
        String eligibilityPayload = String.format(
                "{\"customerId\":%d,\"loanType\":\"PERSONAL\",\"requestedAmount\":60000.00,\"tenureMonths\":12,\"monthlyIncome\":80000.00,\"employmentType\":\"SALARIED\"}",
                customerId);
        givenAuth("officer", "password")
                .body(eligibilityPayload)
                .when().post("/api/loans/eligibility")
                .then().statusCode(200)
                .body("data.eligible", equalTo(true));

        // Step 4: Approve application
        LoanApprovalRequest approvalRequest = new LoanApprovalRequest(
                new BigDecimal("60000.00"), 12, new BigDecimal("10.00"), "Underwriting approval granted");

        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(approvalRequest))
                .when().patch("/api/loans/applications/" + applicationId + "/approve")
                .then().statusCode(200)
                .body("data.status", equalTo("APPROVED"));

        // Step 5: Disburse the loan
        LoanDisbursementRequest disbRequest = new LoanDisbursementRequest(
                new BigDecimal("60000.00"), null, "LN-E2E1-" + uid);

        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(disbRequest))
                .when().post("/api/loans/" + applicationId + "/disburse")
                .then().statusCode(201)
                .body("data.status", equalTo("SUCCESS"));

        // Step 6: Generate and verify EMI schedule
        Response scheduleResult = givenAuth("officer", "password")
                .when().get("/api/loans/" + applicationId + "/schedule")
                .then().statusCode(200)
                .body("data.schedules", hasSize(12))
                .extract().response();

        JsonNode schedNode = objectMapper.readTree(scheduleResult.asString());
        double monthlyEmi = schedNode.get("data").get("monthlyEmi").asDouble();
        assertTrue(monthlyEmi > 5000.0 && monthlyEmi < 6000.0);

        // Step 7: Process monthly repayment
        LoanRepaymentRequest repaymentRequest = new LoanRepaymentRequest(
                "IDEM-E2E-PAY1-" + uid, new BigDecimal("10000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Month 1 Repayment");

        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(repaymentRequest))
                .when().post("/api/loans/" + applicationId + "/repayments")
                .then().statusCode(201)
                .body("data.status", equalTo("SUCCESS"));

        // Step 8: Verify outstanding balance reduced
        Response balanceResult = givenAuth("officer", "password")
                .when().get("/api/loans/" + applicationId + "/balance")
                .then().statusCode(200)
                .extract().response();

        JsonNode balNode = objectMapper.readTree(balanceResult.asString());
        double outstanding = balNode.get("data").get("outstandingPrincipal").asDouble();
        assertTrue(outstanding < 60000.00);

        // Step 9: Pay off remainder to close loan
        LoanRepaymentRequest payoffRequest = new LoanRepaymentRequest(
                "IDEM-E2E-PAYOFF-" + uid, new BigDecimal("55000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Full early payoff");

        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(payoffRequest))
                .when().post("/api/loans/" + applicationId + "/repayments")
                .then().statusCode(201)
                .body("data.loanAccountStatus", equalTo("CLOSED"))
                .body("data.remainingOutstandingPrincipal", equalTo(0.0f));

        // Verify loan application status closed
        givenAuth("officer", "password")
                .when().get("/api/loans/applications/" + applicationId)
                .then().statusCode(200)
                .body("data.status", equalTo("CLOSED"));
    }
}
