# Al Baraka Digital - Secure Banking Platform

## Project Description
**Al Baraka Digital** is a secure digital banking platform designed to manage clients' banking operations such as deposits, withdrawals, and transfers. The platform ensures:

- Secure, traceable, and auditable operations
- Role-based access for clients, bank agents, and administrators
- Automatic validation for low-amount operations and manual validation for high-amount operations
- Stateless authentication using JWT

This platform aims to **eliminate manual errors and fraud risks**, automate banking operations, and provide a foundation for future CI/CD integration.

---

## Objectives
1. **Secure API access** using JWT (stateless authentication).
2. **Implement business logic** for deposits, withdrawals, and transfers.
3. **Workflow validation** for operations above certain amounts (10,000 DH).
4. **Deploy the application** in Docker containers.
5. **Prepare a base** for future CI/CD integration.

---

## Roles and Responsibilities

| Role | Actions |
|------|---------|
| **CLIENT** | Create account, login, create operations, upload justificatifs |
| **AGENT_BANCAIRE** | Consult PENDING operations, approve/reject operations |
| **ADMIN** | Create/manage client and admin accounts, manage account statuses |

---

## Business Scenarios

### 1. Account Creation (Client)
- **Precondition:** Client has no account
- **Action:** Fill registration form (email, password, full name)
- **Result:** Account created with a unique account number

### 2. Login
- **Precondition:** Account exists
- **Action:** Enter email and password
- **Result:** JWT token generated for accessing operations

### 3. Deposit
- **Case A:** ≤ 10,000 DH → automatic validation
- **Case B:** > 10,000 DH → upload justificatif, operation PENDING, agent validation

### 4. Withdrawal
- **Case A:** ≤ 10,000 DH → automatic validation
- **Case B:** > 10,000 DH → upload justificatif, operation PENDING, agent validation

### 5. Transfer
- **Case A:** ≤ 10,000 DH → automatic validation
- **Case B:** > 10,000 DH → upload justificatif, operation PENDING, agent validation

### 6. Agent Management
- Agents consult PENDING operations, verify documents, approve or reject.

### 7. Admin Management
- Admins create/update/delete accounts and set account statuses.

---

## Security
- **JWT Stateless Authentication**
- **Spring Security 6** with custom `UserDetailsService`
- **Password hashing:** BCrypt
- **Endpoint security by role:**
    - CLIENT: `/api/client/**`
    - AGENT_BANCAIRE: `/api/agent/**`
    - ADMIN: `/api/admin/**`

---

## API Endpoints

| Endpoint | Method | Role | Description |
|----------|--------|------|-------------|
| `/auth/login` | POST | All | Authenticate + JWT |
| `/api/client/operations` | POST | CLIENT | Create operation |
| `/api/client/operations/{id}/document` | POST | CLIENT | Upload justificatif |
| `/api/client/operations` | GET | CLIENT | List operations |
| `/api/agent/operations/pending` | GET | AGENT | List PENDING operations |
| `/api/agent/operations/{id}/approve` | PUT | AGENT | Approve operation |
| `/api/agent/operations/{id}/reject` | PUT | AGENT | Reject operation |
| `/api/admin/users` | POST/PUT/DELETE | ADMIN | Manage accounts |

---

## Data Model / Entities

### User
- `id`, `email`, `password`, `fullName`, `role`, `active`, `createdAt`

### Account
- `id`, `accountNumber`, `balance`, `owner`

### Operation
- `id`, `type` (DEPOSIT, WITHDRAWAL, TRANSFER), `amount`, `status` (PENDING, APPROVED, REJECTED), `createdAt`, `validatedAt`, `executedAt`, `accountSource`, `accountDestination`

### Document
- `id`, `fileName`, `fileType`, `storagePath`, `uploadedAt`, `operation`

---