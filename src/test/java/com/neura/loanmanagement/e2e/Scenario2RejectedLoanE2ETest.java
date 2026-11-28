package com.neura.loanmanagement.e2e;

import tools.jackson.databind.JsonNode;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

@DisplayName("E2E Scenario 2: Rejected Loan Workflow")
class Scenario2RejectedLoanE2ETest extends AbstractE2ESupport {

    @Test
    @DisplayName("Create customer -> Submit -> Eligibility (fails) -> Reject -> Disbursement prevented")
    void testRejectedLoanLifecycle() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);

        // 1. Create customer
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                "Riley", "Green", "riley." + uid + "@example.com", "+1-555-8002",
                "NAT-E2E2-" + uid, "600 Reject Blvd", LocalDate.of(1995, 2, 10),
                new BigDecimal("20000.00"), EmploymentType.UNEMPLOYED, 520);

        Response custResult = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(customerRequest))
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().response();

        JsonNode custNode = objectMapper.readTree(custResult.asString());
        long customerId = custNode.get("data").get("id").asLong();

        // 2. Submit application
        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("100000.00"),
                new BigDecimal("12.00"), 12, EmploymentType.UNEMPLOYED, new BigDecimal("20000.00"));

        Response appResult = givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(appRequest))
                .when().post("/api/loans/applications")
                .then().statusCode(201)
                .body("data.status", equalTo("SUBMITTED"))
                .extract().response();

        JsonNode appNode = objectMapper.readTree(appResult.asString());
        long applicationId = appNode.get("data").get("id").asLong();

        // 3. Eligibility check confirms ineligibility
        String eligibilityPayload = String.format(
                "{\"customerId\":%d,\"loanType\":\"PERSONAL\",\"requestedAmount\":100000.00,\"tenureMonths\":12,\"monthlyIncome\":20000.00,\"employmentType\":\"UNEMPLOYED\",\"creditScore\":520}",
                customerId);
        givenAuth("officer", "password")
                .body(eligibilityPayload)
                .when().post("/api/loans/eligibility")
                .then().statusCode(200)
                .body("data.eligible", equalTo(false));

        // 4. Reject application
        LoanRejectionRequest rejectionRequest = new LoanRejectionRequest("Applicant does not meet underwriting criteria");
        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(rejectionRequest))
                .when().patch("/api/loans/applications/" + applicationId + "/reject")
                .then().statusCode(200)
                .body("data.status", equalTo("REJECTED"))
                .body("data.rejectionReason", equalTo("Applicant does not meet underwriting criteria"));

        // 5. Verify that disbursement is prevented on REJECTED loan (409 Conflict)
        LoanDisbursementRequest disbRequest = new LoanDisbursementRequest(
                new BigDecimal("100000.00"), null, "LN-REJ-" + uid);

        givenAuth("officer", "password")
                .body(objectMapper.writeValueAsString(disbRequest))
                .when().post("/api/loans/" + applicationId + "/disburse")
                .then().statusCode(409)
                .body("status", equalTo(409));
    }
}
