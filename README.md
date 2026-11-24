# Legacy Banking Loan Management System

[![CI / Verification Pipeline](https://github.com/neura-org/legacy-loan-management/actions/workflows/ci.yml/badge.svg)](https://github.com/neura-org/legacy-loan-management/actions)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot 2.7.18](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Runtime-blue)
![H2](https://img.shields.io/badge/H2-Test-lightgrey)

An enterprise banking Loan Management System built specifically on **Spring Boot 2.7.18** with **Java 17**. This application serves as a comprehensive, fully executable baseline test application for automated Spring Boot upgrade and migration tooling (Neura Agent).

---

## 1. Technology Stack

| Component | Technology | Version | Management |
|---|---|---|---|
| Runtime | Java OpenJDK | 17 (LTS) | Tooling |
| Framework | Spring Boot | 2.7.18 | Parent POM |
| Security | Spring Security | 5.7.11 | Spring Boot Managed |
| Persistence | Spring Data JPA / Hibernate | 5.6.15.Final | Spring Boot Managed |
| Build Tool | Apache Maven | 3.9.x | Wrapper / CLI |
| Production DB | PostgreSQL | 10+ | Runtime profile |
| In-Memory DB | H2 Database | 2.1.214 | Test profile |
| Migrations | Flyway | 8.5.13 | Spring Boot Managed |
| Testing | JUnit 5 & Mockito | Boot Managed | Spring Boot Managed |
| Contract Tests | Spring Cloud Contract | 3.1.10 (2021.0.9) | Compatible BOM |
| REST Testing | REST Assured | 4.5.1 | Spring Boot Managed |
| Mocking | WireMock | 2.35.2 | Compatible |
| Coverage | JaCoCo | 0.8.11 | Plugin |
| Performance | k6 | Latest | CLI |

---

## 2. Core Functional Modules

- **Customer Management**: Customer onboarding, contact details, identity verification, and KYC status tracking (`PENDING`, `VERIFIED`, `REJECTED`).
- **Loan Applications**: Lifecycle tracking for Personal, Home, Vehicle, and Education loans (`SUBMITTED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `DISBURSED`, `CLOSED`).
- **Eligibility Engine**: Multi-factor underwriting evaluating income minimums, debt-to-income limits, total exposure ratios, and credit score tiers.
- **Disbursement**: One-time loan disbursement creating dedicated accounts and generating complete repayment schedules. Duplicate disbursements are strictly prevented.
- **Reducing-Balance EMI Calculation**: Mathematical reducing-balance formula calculating monthly principal and interest components with exact final installment reconciliation.
- **Repayments & Idempotency**: Full EMI, partial payments, and early payoffs with idempotency key enforcement. Automatically closes the loan account once outstanding principal reaches $0.00.
- **External Integration Simulation**: WireMock credit score simulation supporting retries, transient 503 handling, client error handling, and timeout limits.
- **Security & Multi-Tenancy**: HTTP Basic authentication with role-based access control (`ADMIN`, `LOAN_OFFICER`, `CUSTOMER`) and customer-level data isolation.

---

## 3. Getting Started & Local Setup

### Prerequisites
- Java Development Kit (JDK) 17 installed and configured (`JAVA_HOME`).
- Apache Maven 3.9+ (or use `./mvnw`).
- PostgreSQL (for `dev` profile) or run tests directly with in-memory H2 (`test` profile).

### Build Commands
```bash
# Clean and compile
mvn clean compile

# Run unit and contract tests
mvn test

# Run complete verification including integration tests and JaCoCo coverage
mvn verify

# Complete clean build and verification
mvn clean verify
```

### Running Locally
```bash
# Run with default dev profile (PostgreSQL on localhost:5432)
mvn spring-boot:run

# Run with test profile (H2 in-memory, no external database required)
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

The application starts on port `8080`.

---

## 4. Default Application Users & Roles

| Username | Password | Role | Description |
|---|---|---|---|
| `admin` | `password` | `ADMIN` | Full administrative access to all endpoints |
| `officer` | `password` | `LOAN_OFFICER` | Review, approve, reject, disburse loans |
| `john_doe` | `password` | `CUSTOMER` | Customer 1 (accesses only own records) |
| `jane_smith` | `password` | `CUSTOMER` | Customer 2 (accesses only own records) |

---

## 5. Documentation Links

- [Architecture Guide](docs/ARCHITECTURE.md)
- [REST API Reference](docs/API.md)
- [Testing & Quality Guide](docs/TESTING.md)
- [Database Schema & Migrations](docs/DATABASE.md)

---

## 6. Migration Testing Baseline

This project is preserved at **Spring Boot 2.7.18** with **Java 17** to serve as an authoritative benchmark for the **Neura Agent** automated migration pipeline. All test suites pass cleanly on this baseline before any upgrade is initiated.
