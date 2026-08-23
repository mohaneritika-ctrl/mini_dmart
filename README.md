# Mini D-Mart — Full-Stack Grocery Store Application

[![Backend Build](https://img.shields.io/badge/Backend-Spring%20Boot%203.3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Frontend Build](https://img.shields.io/badge/Frontend-React%2018%20%7C%20Vite-blue.svg)](https://vitejs.dev/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2015+-336791.svg)](https://www.postgresql.org/)
[![Security](https://img.shields.io/badge/Security-JWT%20%7C%20RBAC%20%7C%20BCrypt-red.svg)](https://spring.io/projects/spring-security)

An enterprise-grade, full-stack grocery store web application featuring role-based portals, isolated customer carts, concurrency-safe checkout and stock management, order cancellation, and full-lifecycle return and exchange processing.

---

## 1. Project Overview & Architecture

Mini D-Mart is engineered with a decoupled client-server architecture:
- **Backend:** Spring Boot 3 with layered domain architecture (Controller $\rightarrow$ DTO $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ JPA Entity), Spring Security 6 with stateless JWT authentication, and PostgreSQL.
- **Frontend:** React 18 SPA powered by Vite, utilizing Context API for auth and cart state, custom responsive CSS (no heavy external UI bloat), and client-side routing.

```
┌─────────────────────────────────────────────────────────────┐
│                    React 18 Frontend                        │
│   (Vite, React Router v6, Context API, Mini D-Mart CSS)     │
└──────────────────────────────┬──────────────────────────────┘
                               │  REST APIs + JWT Bearer
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot 3 Backend                      │
│   ┌─────────────────────────────────────────────────────┐   │
│   │ Controllers (REST Endpoints & DTO Validation)       │   │
│   ├─────────────────────────────────────────────────────┤   │
│   │ Security Layer (JWT Filter, RBAC @PreAuthorize)     │   │
│   ├─────────────────────────────────────────────────────┤   │
│   │ Service Layer (Business Logic & Transactions)       │   │
│   ├─────────────────────────────────────────────────────┤   │
│   │ Data Access (Spring Data JPA & Pessimistic Locks)   │   │
│   └─────────────────────────────────────────────────────┘   │
└──────────────────────────────┬──────────────────────────────┘
                               │  JDBC Connection Pool (Hikari)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  PostgreSQL Database                        │
│   (Users, Categories, Products, Carts, Orders, Returns)     │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Frontend** | React 18, Vite, JavaScript ES6+ | Fast single-page application |
| **Routing & State** | React Router v6, React Context | Routing and global state management |
| **Styling** | Custom CSS3 | Modern, responsive Mini D-Mart theme |
| **Backend** | Java 21 LTS, Spring Boot 3.3.x | Enterprise REST backend |
| **Security** | Spring Security 6, JJWT, BCrypt | Token authentication and RBAC |
| **Persistence** | Spring Data JPA, Hibernate ORM | Relational data access and transactions |
| **Database** | PostgreSQL 15+ | Relational data persistence |
| **Build Tools** | Maven 3.9+ (Backend), NPM (Frontend) | Compilation, testing, and packaging |

---

## 3. User Roles & Capabilities

### 🛒 Customer (`ROLE_CUSTOMER`)
- Self-registration, secure login, and profile view.
- Browse categories and product catalog with search, price filters, and stock tags.
- Manage shopping cart with real-time dynamic pricing and stock validation.
- Checkout with **Store Pickup** (time slot selection) or **Home Delivery** (address).
- View order history and line-item snapshots.
- Cancel eligible early-stage orders (`PLACED`/`CONFIRMED`) with instant stock restoration.
- Request Returns or Exchanges on delivered orders within 7 days.

### 👔 Staff (`ROLE_STAFF`)
- Access dedicated Staff Operations Dashboard.
- Monitor incoming orders and advance fulfillment states (`CONFIRMED` $\rightarrow$ `PREPARING` $\rightarrow$ `READY_FOR_PICKUP` / `OUT_FOR_DELIVERY` $\rightarrow$ `COMPLETED`).
- Review Return & Exchange requests, inspect customer remarks, approve, reject with staff comment, and complete with inventory sync.

### ⚙️ Admin (`ROLE_ADMIN`)
- Access Admin Management Portal.
- Full CRUD management of Categories and Product Catalog.
- Warehouse Stock Management with instant inventory adjustment.
- Global store-wide order monitoring.
- Store-wide Return & Exchange lifecycle governance.

---

## 4. Return & Exchange Lifecycle

```
Customer Submits Request (Order COMPLETED & <= 7 Days)
                      │
                      ▼
             Status: REQUESTED  (No inventory modified)
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
Status: REJECTED            Status: APPROVED  (No inventory modified)
(No inventory modified)             │
                                    ▼
                           Status: COMPLETED
                ┌───────────────────┴───────────────────┐
                ▼                                       ▼
        Type = RETURN                           Type = EXCHANGE
 (Restores +Qty to Stock)             (Checks & Deducts -Qty Replacement)
```

---

## 5. API Endpoints Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Customer self-registration |
| `POST` | `/api/auth/login` | Public | User authentication & JWT issuance |

### Product & Category Management (`/api/products`, `/api/categories`)
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/categories` | Public / All | List categories |
| `POST` | `/api/categories` | STAFF, ADMIN | Create category |
| `PUT` | `/api/categories/{id}` | STAFF, ADMIN | Update category |
| `DELETE` | `/api/categories/{id}` | ADMIN | Delete category |
| `GET` | `/api/products` | Public / All | Paginated & filtered products |
| `GET` | `/api/products/{id}` | Public / All | Product details |
| `POST` | `/api/products` | STAFF, ADMIN | Create product |
| `PUT` | `/api/products/{id}` | STAFF, ADMIN | Update product |
| `PATCH` | `/api/products/{id}/stock` | STAFF, ADMIN | Adjust stock quantity |
| `PATCH` | `/api/products/{id}/status` | STAFF, ADMIN | Toggle active status |
| `DELETE` | `/api/products/{id}` | ADMIN | Delete product |

### Shopping Cart (`/api/cart`)
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/cart` | CUSTOMER | View customer cart |
| `POST` | `/api/cart/items` | CUSTOMER | Add product to cart |
| `PUT` | `/api/cart/items/{id}` | CUSTOMER | Update item quantity |
| `DELETE` | `/api/cart/items/{id}` | CUSTOMER | Remove item from cart |
| `DELETE` | `/api/cart` | CUSTOMER | Empty shopping cart |

### Order Management (`/api/orders`, `/api/staff/orders`, `/api/admin/orders`)
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/orders/checkout` | CUSTOMER | Place order with stock deduction |
| `GET` | `/api/orders` | CUSTOMER | Order history |
| `GET` | `/api/orders/{id}` | CUSTOMER | Order details snapshot |
| `PUT` | `/api/orders/{id}/cancel` | CUSTOMER | Cancel order & restore stock |
| `GET` | `/api/staff/orders` | STAFF, ADMIN | Orders processing queue |
| `PUT` | `/api/staff/orders/{id}/status` | STAFF, ADMIN | Update order status |
| `GET` | `/api/admin/orders` | ADMIN | All store orders |

### Returns & Exchanges (`/api/returns`, `/api/staff/returns`, `/api/admin/returns`)
| Method | Endpoint | Access | Purpose |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/returns` | CUSTOMER | Submit return/exchange request |
| `GET` | `/api/returns` | CUSTOMER | Customer request history |
| `GET` | `/api/returns/{id}` | CUSTOMER | Customer request details |
| `PUT` | `/api/returns/{id}/cancel` | CUSTOMER | Cancel pending request |
| `GET` | `/api/staff/returns` | STAFF, ADMIN | Returns queue with filters |
| `PUT` | `/api/staff/returns/{id}/approve` | STAFF, ADMIN | Approve request |
| `PUT` | `/api/staff/returns/{id}/reject` | STAFF, ADMIN | Reject with comment |
| `PUT` | `/api/staff/returns/{id}/complete` | STAFF, ADMIN | Complete and update inventory |
| `GET` | `/api/admin/returns` | ADMIN | Global returns overview |
| `PUT` | `/api/admin/returns/{id}/status` | ADMIN | Update request status |

---

## 6. Local Setup & Execution Guide

### Prerequisites
- **Java 21 LTS**
- **Node.js 18+** & **npm**
- **PostgreSQL 15+**

### 1. Database Setup
Create the PostgreSQL database:
```sql
CREATE DATABASE dmart_db;
```

### 2. Configure Backend Environment
Create `backend/.env` (or set environment variables):
```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/dmart_db
DB_USERNAME=postgres
DB_PASSWORD=your_password_here
JWT_SECRET=your_base64_256bit_secret_here
JWT_EXPIRATION=86400000
CORS_ORIGIN=http://localhost:5173
```

### 3. Start Backend
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
Backend runs at `http://localhost:8080`.

### 4. Start Frontend
```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```
Open **http://localhost:5173** in your browser.

---

## 7. Verification & Testing

### Run Backend Integration Test Suite (51 Tests):
```powershell
cd backend
.\mvnw.cmd test
```
**Expected Output:**
```
[INFO] Results:
[INFO] Tests run: 51, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Build Frontend Production Bundle:
```powershell
cd frontend
npm.cmd run build
```
**Expected Output:**
```
✓ built in ~1-2s (dist/ generated cleanly)
```

---

## 8. Project Demonstration Script (5–10 Minutes)

1. **Introduction (1 min):** High-level overview of Mini D-Mart architecture, PostgreSQL persistence, and Spring Security + JWT RBAC.
2. **Customer Journey (3 mins):**
   - Register a new customer account and log in.
   - Browse catalog, search products, add items to cart.
   - Adjust cart quantities and complete Checkout (Store Pickup / Home Delivery).
   - View Order History and inspect item price snapshots.
3. **Order Cancellation & Stock Safety (1 min):**
   - Place an order and cancel it while in `PLACED` state.
   - Verify inventory is immediately restored to database.
4. **Return & Exchange Workflow (2 mins):**
   - Submit a return request on a completed delivered order within the 7-day window.
   - Log in as Staff (`STAFF`), review request queue, approve request, and complete it.
   - Verify stock is automatically synchronized.
5. **Admin Portal & Security (2 mins):**
   - Log in as Admin (`ADMIN`), demonstrate Category CRUD, Product editing, Inventory management, and global returns monitoring.
   - Demonstrate IDOR security (Customer cannot view other customers' orders).