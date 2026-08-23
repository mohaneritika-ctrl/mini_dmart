# Mini D-Mart — Security Policy & Architecture

## 1. Supported Versions

| Version | Supported |
| :--- | :--- |
| 1.0.0 (Phases 1–10) | :white_check_mark: |
| < 1.0.0 | :x: |

---

## 2. Core Security Architecture & Principles

Mini D-Mart implements enterprise-grade security protocols across all application layers:

### A. Authentication & Password Security
- **Stateless Authentication:** Stateless JSON Web Tokens (JWT) signed with HMAC-SHA algorithms.
- **Password Hashing:** Passwords are encrypted with BCrypt (10 rounds work factor) before storage. Plaintext passwords or hashes are never returned across any API responses.
- **Expiration & Tampering Protection:** Tokens carry cryptographically validated expiration timestamps. Tampered or expired tokens are rejected at filter level with `401 Unauthorized`.

### B. Role-Based Access Control (RBAC)
Strict segregation of privileges across three primary roles:
- **`CUSTOMER`**:
  - Self-owned profile, shopping cart, order placement, order history, and return/exchange requests.
  - Zero access to staff operations or administrative catalog/inventory management.
- **`STAFF`**:
  - Access to store order queues, fulfillment updates, and return/exchange approval/rejection/completion.
  - Restricted from administrative operations (e.g. deleting products or categories).
- **`ADMIN`**:
  - Complete administrative authority over category CRUD, product catalog CRUD, warehouse stock inventory management, order oversight, and global return/exchange management.
- **Backend Authorization as Source of Truth:** `@PreAuthorize` method security protects every controller method. Frontend route guards provide navigation UX only.

### C. Insecure Direct Object Reference (IDOR) Protection
- Customer orders (`/api/orders/{id}`) and return requests (`/api/returns/{id}`) are verified against the authenticated user's ID (`findByIdAndUserId`).
- Requests attempting to access or modify resources belonging to another user are blocked and return `404 Not Found` without leaking data existence.

### D. Concurrency-Safe Inventory & Price Integrity
- **Pessimistic Write Locking (`PESSIMISTIC_WRITE`):** Concurrent checkouts, cancellations, and return/exchange completion operate under atomic database-level row locks.
- **Zero Client-Trust Pricing:** Cart and checkout calculate totals directly from database price snapshots, completely preventing client-side price manipulation.

### E. Secret & Credential Management
- Zero hardcoded passwords or API keys in source code or documentation.
- Database credentials, JWT signing secrets, server ports, and CORS origins are loaded via environment variables with safe development defaults.
- Real credentials are never committed to version control; `.env.example` provides documentation templates.

### F. Global Exception Handling & Data Leakage Prevention
- Centralized `GlobalExceptionHandler` intercepts all validation, authentication, conflict, and server exceptions.
- Consistent JSON error schema (`status`, `message`, `timestamp`). Internal stack traces, package structures, and SQL queries are completely suppressed.

---

## 3. Reporting a Vulnerability

If you discover any security issue or vulnerability within Mini D-Mart:
1. Please **do NOT** open a public issue.
2. Report the vulnerability privately to the project security maintainers with reproduction steps.
3. The security team will investigate, patch the issue, and release an update.