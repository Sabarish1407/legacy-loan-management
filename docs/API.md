# REST API Specification

Base URL: `http://localhost:8080`

Authentication: HTTP Basic Auth (`Authorization: Basic <base64>`)

Roles:
- `ADMIN`: `admin` / `password`
- `LOAN_OFFICER`: `officer` / `password`
- `CUSTOMER`: `<customer-email>` / `password` (e.g., `john_doe` / `password`)

---

## 1. Customer Management

### Create Customer
- **Endpoint**: `POST /api/customers`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`
- **Request Body**:
```json
{
  "firstName": "Alex",
  "lastName": "Taylor",
  "email": "alex.taylor@example.com",
  "phone": "+1-555-1234",
  "nationalId": "NAT-10099",
  "address": "100 Financial Blvd",
  "dateOfBirth": "1990-05-15",
  "monthlyIncome": 75000.00,
  "employmentType": "SALARIED",
  "creditScore": 750
}
```
- **Response**: `201 Created`

### Get Customer by ID
- **Endpoint**: `GET /api/customers/{id}`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`, `CUSTOMER` (own ID only)
- **Response**: `200 OK`

### Update Customer
- **Endpoint**: `PUT /api/customers/{id}`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`, `CUSTOMER` (own ID only)
- **Response**: `200 OK`

### Update Customer KYC
- **Endpoint**: `PATCH /api/customers/{id}/kyc?status=VERIFIED`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`
- **Response**: `200 OK`

---

## 2. Loan Application Management

### Submit Loan Application
- **Endpoint**: `POST /api/loans/applications`
- **Authorized Roles**: Authenticated
- **Request Body**:
```json
{
  "customerId": 1,
  "loanType": "PERSONAL",
  "requestedAmount": 50000.00,
  "interestRate": 10.00,
  "tenureMonths": 12,
  "employmentType": "SALARIED",
  "monthlyIncome": 75000.00
}
```
- **Response**: `201 Created`

### Get Loan Application by ID
- **Endpoint**: `GET /api/loans/applications/{id}`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`, `CUSTOMER` (own application only)
- **Response**: `200 OK`

### Get Customer Loans
- **Endpoint**: `GET /api/customers/{customerId}/loans`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`, `CUSTOMER` (own customer ID only)
- **Response**: `200 OK`

### Approve Loan Application
- **Endpoint**: `PATCH /api/loans/applications/{id}/approve`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`
- **Request Body** (optional):
```json
{
  "approvedAmount": 50000.00,
  "approvedTenureMonths": 12,
  "approvedInterestRate": 10.00,
  "reviewNotes": "Approved by credit committee"
}
```
- **Response**: `200 OK`

### Reject Loan Application
- **Endpoint**: `PATCH /api/loans/applications/{id}/reject`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`
- **Request Body**:
```json
{
  "reason": "Debt-to-income ratio exceeds threshold"
}
```
- **Response**: `200 OK`

---

## 3. Loan Disbursement

### Disburse Loan
- **Endpoint**: `POST /api/loans/{loanId}/disburse`
- **Authorized Roles**: `ADMIN`, `LOAN_OFFICER`
- **Request Body** (optional):
```json
{
  "amount": 50000.00,
  "accountNumber": "LN-2026-001"
}
```
- **Response**: `201 Created`

---

## 4. Repayments & Schedules

### Process Repayment
- **Endpoint**: `POST /api/loans/{loanId}/repayments`
- **Headers**: `Idempotency-Key: <unique-uuid>` (or in body)
- **Authorized Roles**: Authenticated (own loan account)
- **Request Body**:
```json
{
  "idempotencyKey": "PAY-UUID-12345",
  "amount": 4395.79,
  "paymentMethod": "SIMULATED_GATEWAY",
  "notes": "Month 1 installment"
}
```
- **Response**: `201 Created`

### Get Repayment History
- **Endpoint**: `GET /api/loans/{loanId}/repayments`
- **Authorized Roles**: Authenticated (own loan account)
- **Response**: `200 OK`

### Get Repayment Schedule
- **Endpoint**: `GET /api/loans/{loanId}/schedule`
- **Authorized Roles**: Authenticated (own loan account)
- **Response**: `200 OK`

### Get Outstanding Balance
- **Endpoint**: `GET /api/loans/{loanId}/balance`
- **Authorized Roles**: Authenticated (own loan account)
- **Response**: `200 OK`

---

## 5. Eligibility Engine

### Assess Eligibility
- **Endpoint**: `POST /api/loans/eligibility`
- **Authorized Roles**: Authenticated
- **Request Body**:
```json
{
  "customerId": 1,
  "loanType": "PERSONAL",
  "requestedAmount": 50000.00,
  "tenureMonths": 12,
  "monthlyIncome": 75000.00,
  "employmentType": "SALARIED",
  "creditScore": 750
}
```
- **Response**: `200 OK`
