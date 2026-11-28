package com.neura.loanmanagement.e2e;

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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("E2E Scenario 3: Repayment Failure and Consistency")
class Scenario3RepaymentFailureE2ETest extends AbstractE2ESupport {

    @Test
    @DisplayName("Create/Approve/Disburse loan -> Submit failing payment -> Verify 422 error & balance unchanged")
    void testRepaymentFailureWorkflow() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // 1. Create customer and application
        CreateCustomerRequest custRequest = new CreateCustomerRequest(
                "Dakota", "Hayes", "dakota." + uid + "@example.com", "+1-555-8003",
                "NAT-E2E3-" + uid, "700 Edge Road", LocalDate.of(1991, 7, 11),
                new BigDecimal("90000.00"), EmploymentType.SALARIED, 770);

        Response custResult = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(custRequest))
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().response();

        long customerId = objectMapper.readTree(custResult.asString()).get("data").get("id").asLong();

        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("40000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("90000.00"));

        Response appResult = givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(appRequest))
                .when().post("/api/loans/applications")
                .then().statusCode(201)
                .extract().response();

        long applicationId = objectMapper.readTree(appResult.asString()).get("data").get("id").asLong();

        // Approve loan
        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(new LoanApprovalRequest(new BigDecimal("40000.00"), 12, new BigDecimal("10.00"), "OK")))
                .when().patch("/api/loans/applications/" + applicationId + "/approve")
                .then().statusCode(200);

        // 2. Disburse loan
        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(new LoanDisbursementRequest(new BigDecimal("40000.00"), null, "LN-FAIL-" + uid)))
                .when().post("/api/loans/" + applicationId + "/disburse")
                .then().statusCode(201);

        // Check initial balance
        Response balBefore = givenAuth("officer", "password")
                .when().get("/api/loans/" + applicationId + "/balance")
                .then().statusCode(200)
                .extract().response();
        double balanceBefore = objectMapper.readTree(balBefore.asString()).get("data").get("outstandingPrincipal").asDouble();

        // 3. Submit invalid repayment (trigger simulated gateway failure via special instruction)
        LoanRepaymentRequest invalidRepayment = new LoanRepaymentRequest(
                "IDEM-FAIL-" + uid, new BigDecimal("5000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "SIMULATE_GATEWAY_FAILURE");

        // 4. Verify appropriate error response (422 Unprocessable Entity)
        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(invalidRepayment))
                .when().post("/api/loans/" + applicationId + "/repayments")
                .then().statusCode(422)
                .body("status", equalTo(422));

        // 5. Verify database consistency: outstanding balance remains unchanged
        Response balAfter = givenAuth("officer", "password")
                .when().get("/api/loans/" + applicationId + "/balance")
                .then().statusCode(200)
                .extract().response();
        double balanceAfter = objectMapper.readTree(balAfter.asString()).get("data").get("outstandingPrincipal").asDouble();

        assertEquals(balanceBefore, balanceAfter, 0.001, "Outstanding balance must not change on failed payment transaction");
    }
}
