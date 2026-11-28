package com.neura.loanmanagement.e2e;

import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@DisplayName("E2E Scenario 4: Customer Data Isolation")
class Scenario4CustomerIsolationE2ETest extends AbstractE2ESupport {

    @Test
    @DisplayName("Create 2 customers with loans -> Customer 1 attempts to access Customer 2 loan -> 403 Forbidden")
    void testCustomerDataIsolation() throws Exception {
        String uid = UUID.randomUUID().toString().substring(0, 8);
        String cust1Email = "cust1." + uid + "@example.com";

        // 1. Create Customer 1
        CreateCustomerRequest cust1Req = new CreateCustomerRequest(
                "CustOne", "Alpha", cust1Email, "+1-555-9001",
                "NAT-ISO1-" + uid, "101 Alpha St", LocalDate.of(1990, 1, 1),
                new BigDecimal("70000.00"), EmploymentType.SALARIED, 750);

        Response c1Res = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(cust1Req))
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().response();
        long cust1Id = objectMapper.readTree(c1Res.asString()).get("data").get("id").asLong();

        // 1. Create Customer 2
        CreateCustomerRequest cust2Req = new CreateCustomerRequest(
                "CustTwo", "Beta", "cust2." + uid + "@example.com", "+1-555-9002",
                "NAT-ISO2-" + uid, "202 Beta St", LocalDate.of(1992, 2, 2),
                new BigDecimal("85000.00"), EmploymentType.SALARIED, 780);

        Response c2Res = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(cust2Req))
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().response();
        long cust2Id = objectMapper.readTree(c2Res.asString()).get("data").get("id").asLong();

        // 2. Create Loan for Customer 1
        LoanApplicationRequest app1Req = new LoanApplicationRequest(
                cust1Id, LoanType.PERSONAL, new BigDecimal("30000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        Response app1Res = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(app1Req))
                .when().post("/api/loans/applications")
                .then().statusCode(201)
                .extract().response();
        long app1Id = objectMapper.readTree(app1Res.asString()).get("data").get("id").asLong();

        // 2. Create Loan for Customer 2
        LoanApplicationRequest app2Req = new LoanApplicationRequest(
                cust2Id, LoanType.PERSONAL, new BigDecimal("50000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("85000.00"));
        Response app2Res = givenAuth("admin", "password")
                .body(objectMapper.writeValueAsString(app2Req))
                .when().post("/api/loans/applications")
                .then().statusCode(201)
                .extract().response();
        long app2Id = objectMapper.readTree(app2Res.asString()).get("data").get("id").asLong();

        // 3. Authenticate as Customer 1
        givenAuth(cust1Email, "password")
                .when().get("/api/customers/" + cust1Id)
                .then().statusCode(200);

        givenAuth(cust1Email, "password")
                .when().get("/api/loans/applications/" + app1Id)
                .then().statusCode(200);

        // 4 & 5. Attempt to access Customer 2's profile -> 403 Forbidden
        givenAuth(cust1Email, "password")
                .when().get("/api/customers/" + cust2Id)
                .then().statusCode(403);

        // Attempt to access Customer 2's loans -> 403 Forbidden
        givenAuth(cust1Email, "password")
                .when().get("/api/customers/" + cust2Id + "/loans")
                .then().statusCode(403);

        // Attempt to access Customer 2's loan application -> 403 Forbidden
        givenAuth(cust1Email, "password")
                .when().get("/api/loans/applications/" + app2Id)
                .then().statusCode(403);

        // Attempt to approve loan (administrative endpoint) -> 403 Forbidden
        givenAuth(cust1Email, "password")
                .body("{}")
                .when().patch("/api/loans/applications/" + app1Id + "/approve")
                .then().statusCode(403);
    }
}
