# Testing Documentation

The test suite covers unit tests, integration tests, API contract tests, security tests, end-to-end workflow scenarios, and k6 performance benchmarks.

---

## 1. Test Architecture & Categories

### A. Unit Tests (`com.neura.loanmanagement.unit`)
- **Framework**: JUnit 5 + Mockito
- **Scope**:
  - `EmiCalculatorServiceTest`: Reducing-balance formula, zero-interest rate loans, 1-month boundary, long tenure mortgages, invalid inputs, schedule generation integrity.
  - `LoanEligibilityServiceTest`: Multi-factor underwriting rules, income minimums, credit score thresholds, debt-to-income limits, tier rate assignments.
  - `LoanApplicationServiceTest`: Submission, approval, rejection, state machine validation (`InvalidStateTransitionException`).
  - `LoanDisbursementServiceTest`: Disbursement generation, schedule linkage, duplicate prevention (`DuplicateDisbursementException`).
  - `LoanRepaymentServiceTest`: Full payments, partial payments, loan closure, balance reduction, idempotency enforcement.

### B. Integration Tests (`com.neura.loanmanagement.integration`)
- **Database**: In-memory H2 with PostgreSQL compatibility mode (`application-test.yml`)
- **Scope**:
  - `ApplicationStartupIT`: Spring context initialization, Flyway migration execution, H2 dialect verification.
  - `CustomerRepositoryIT`: CRUD operations, derived JPA query methods, uniqueness lookups.
  - `LoanPersistenceIT`: Multi-table relational graph persistence and cascading.
  - `TransactionRollbackIT`: Rollback validation on exceptions.
  - `DatabaseConstraintsIT`: Unique constraints on email, national ID, and idempotency key.

### C. Contract Tests (`src/test/resources/contracts` & `com.neura.loanmanagement.contract`)
- **Framework**: Spring Cloud Contract 3.1.10 (Release Train 2021.0.9)
- **Contracts**:
  1. `customer_creation.yml`
  2. `loan_application.yml`
  3. `loan_approval.yml`
  4. `loan_rejection.yml`
  5. `loan_disbursement.yml`
  6. `emi_schedule.yml`
  7. `loan_repayment.yml`
  8. `outstanding_balance.yml`

### D. API Tests (`com.neura.loanmanagement.api`)
- **Framework**: MockMvc + Jackson
- **Scope**: HTTP status codes, validation payloads, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict.

### E. End-to-End Tests (`com.neura.loanmanagement.e2e`)
- `Scenario1SuccessfulLoanLifecycleE2ETest`: Full 9-step happy path from registration to loan closure.
- `Scenario2RejectedLoanE2ETest`: Submitting an ineligible application, rejecting it, and asserting disbursement is blocked.
- `Scenario3RepaymentFailureE2ETest`: Simulating gateway rejection, verifying error handling and balance consistency.
- `Scenario4CustomerIsolationE2ETest`: Two separate customers, verifying cross-customer access attempts receive 403 Forbidden.

### F. Security Tests (`com.neura.loanmanagement.security`)
- Role authorization, HTTP Basic authentication, password protection in error responses, data isolation.

### G. External Integration Simulation (`com.neura.loanmanagement.external`)
- WireMock simulated credit bureau verifying timeouts, retries, 503 backoff, and 400 error handling.

---

## 2. Test Execution Commands

```bash
# Run unit tests only
mvn test -Dtest="com.neura.loanmanagement.unit.*Test"

# Run integration tests
mvn verify -Dtest="none" -Dit.test="*IT"

# Run contract tests
mvn test -Dtest="ContractVerifierTest"

# Run E2E tests
mvn verify -Dtest="none" -Dit.test="*E2ETest"

# Run full verification with JaCoCo code coverage
mvn clean verify
```

---

## 3. Performance Testing (k6)

Run benchmarks against a running application instance:
```bash
# Run all scenarios with summary output
k6 run k6/run-all-performance-tests.js

# Specific operations with custom virtual users and duration
k6 run -e VUS=20 -e DURATION=30s k6/customer-lookup.js
k6 run -e VUS=10 -e DURATION=30s k6/loan-application.js
k6 run -e VUS=10 -e DURATION=30s k6/repayment-processing.js
```
