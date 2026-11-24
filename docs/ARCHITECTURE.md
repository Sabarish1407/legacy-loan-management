# Legacy Loan Management Architecture

## 1. System Overview

The **Legacy Loan Management System** (`legacy-loan-management`) is an enterprise-grade banking application built on **Spring Boot 2.7.18** with **Java 17**. It manages the end-to-end lifecycle of loan applications: from customer creation and KYC, eligibility assessment, underwriting approval/rejection, loan disbursement, reducing-balance EMI calculation, repayment processing with idempotency, through to final payoff and loan closure.

## 2. Layered Architecture

The application strictly adheres to a domain-driven layered architecture:

- **Controller (`com.neura.loanmanagement.controller`)**: Exposes RESTful endpoints conforming to HTTP standards. Maps HTTP requests into strongly typed DTOs and handles response serialization.
- **Service (`com.neura.loanmanagement.service`)**: Core business workflows, transaction boundaries (`@Transactional`), eligibility rules, and schedule math.
- **Repository (`com.neura.loanmanagement.repository`)**: Spring Data JPA repositories with custom derived and JPQL queries.
- **Entity (`com.neura.loanmanagement.entity`)**: Relational JPA entities representing the persistent banking domain.
- **DTO (`com.neura.loanmanagement.dto`)**: Request and response data transfer objects. JPA entities are never leaked over REST APIs.
- **Mapper (`com.neura.loanmanagement.mapper`)**: Clean transformation layer between entities and DTOs.
- **Security (`com.neura.loanmanagement.security`)**: Spring Security 2.7.18 HTTP Basic authentication, role-based access control (ADMIN, LOAN_OFFICER, CUSTOMER), and tenant/customer-level data isolation.
- **Integration (`com.neura.loanmanagement.integration`)**: Simulated integrations with external credit bureaus and payment gateways.
- **Exception (`com.neura.loanmanagement.exception`)**: Global REST exception handler mapping business violations to standard RFC-7807/JSON error representations.
- **Scheduler (`com.neura.loanmanagement.scheduler`)**: Background scheduled jobs for detecting overdue installments.
- **Validation (`com.neura.loanmanagement.validation`)**: Custom bean validation constraints (e.g., `@ValidTenure`).

## 3. Technology Stack Summary

| Component | Technology | Version |
|---|---|---|
| Language | Java | 17 (Eclipse Temurin) |
| Framework | Spring Boot | 2.7.18 |
| Security | Spring Security | 5.7.11 (Boot managed) |
| Persistence | Spring Data JPA / Hibernate | 5.6.15.Final (Boot managed) |
| Database Migrations | Flyway | 8.5.13 (Boot managed) |
| Runtime DB | PostgreSQL | PostgreSQL 10+ |
| Testing DB | H2 (in-memory, PostgreSQL mode) | 2.1.214 (Boot managed) |
| Contract Testing | Spring Cloud Contract | 3.1.10 (Release Train 2021.0.9) |
| External Simulation | WireMock | 2.35.2 |
| Coverage | JaCoCo | 0.8.11 |
| Load Testing | k6 | Latest |

## 4. Key Design Decisions

1. **State Machine Integrity**: Permitted application transitions are enforced deterministically:
   - `SUBMITTED` -> `UNDER_REVIEW`, `APPROVED`, `REJECTED`
   - `UNDER_REVIEW` -> `APPROVED`, `REJECTED`
   - `APPROVED` -> `DISBURSED`, `REJECTED`
   - `DISBURSED` -> `CLOSED`
2. **Idempotency**: All repayments mandate an `Idempotency-Key` (header or payload). Duplicate submissions return HTTP 409 Conflict without altering account balances.
3. **Reducing-Balance Calculation**: MathContext-precision EMI calculation ensures rounding adjustments land cleanly on the final installment, keeping principal accounting exact to $0.00.
