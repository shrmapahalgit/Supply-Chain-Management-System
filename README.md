# FoodFlow — Food Supply Chain Management System

An advanced food logistics and supply chain operations platform built with **Java 21, Spring Boot 3.4, Spring Security, JDBC, MySQL, HTML5, CSS3 and JavaScript**.

FoodFlow pairs high-performance server-side data processing with an elegant, responsive frontend matching FoodFlow's signature **forest-green** and **sage-green** visual identity.

---

## 🌟 Key Features

### 🔐 Security & Access Control (New in V2)
- **Spring Security Integration**: Session-based authentication protecting the application shell and all operational REST APIs.
- **BCrypt Password Hashing**: Passwords stored as one-way cryptographic hashes; plain-text passwords never touch the database.
- **Public Operator Registration**: Easy sign-up defaulting securely to `ROLE_USER` (`Supply Chain Operator`). No hardcoded admin credentials.
- **CSRF Defense**: Automatic `CookieCsrfTokenRepository` integration with `X-XSRF-TOKEN` request header protection for all mutating operations (`POST`, `PUT`, `DELETE`, `PATCH`).
- **Session Guard & Working Logout**: Active session check on page load with automatic redirection upon session expiration, and one-click secure sign-out.

### 📦 Supply Chain & Operations
- **Real-Time Operations Dashboard**: High-level counters for total products tracked, active suppliers, total purchase orders, and low-stock items.
- **Inventory Catalogue (CRUD)**: Create, inspect, edit, and delete food items with SKU uniqueness, category assignment, expiration dates, unit pricing, and reorder levels.
- **Supplier Directory (CRUD)**: Maintain sourcing partners, contact people, email addresses, phone numbers, cities, and operational statuses (`ACTIVE` / `INACTIVE`).
- **Purchase Order Lifecycle**: Generate purchase orders with automatic reference code generation (`PO-YYYY-XXXX`), line total calculations, supplier linkage, and turn-by-turn status updates (`PENDING` ➔ `APPROVED` ➔ `IN_TRANSIT` ➔ `DELIVERED` ➔ `CANCELLED`).
- **1-Click Reorder Action**: Instant replenishment trigger on low-stock watchlist items that pre-populates purchase orders.
- **Multi-Criteria Filtering & Search**: Instant client-side search across all tables plus category filters for inventory and status filters for orders.
- **CSV Data Export**: One-click browser CSV downloads for inventory catalogues, supplier directories, and purchase orders.

---

## 💻 System Requirements

- **Java JDK 21+** (`java -version`)
- **Apache Maven 3.9+** (`mvn -version`)
- **MySQL Server 8.x** (`mysql --version`)
- Modern Web Browser (Chrome, Firefox, Safari, Edge)

---

## 🚀 Beginner Quick-Start Guide

Follow these exact terminal commands from the project root directory (`FoodSupplyChainManagement`):

### Step 1: Start MySQL
If installed via Homebrew on macOS:
```bash
brew services start mysql
```
*(Or start MySQL via the MySQL Preference Pane or system service on your OS).*

### Step 2: Run the Non-Destructive Database Migration
If you already have the existing `food_supply_chain` database, apply the safe `v2_users.sql` migration (this will never drop or alter existing records):
```bash
mysql -u root food_supply_chain < database/v2_users.sql
```

*(Note: For a brand new machine with no existing database, create it and import the full schema):*
```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS food_supply_chain;"
mysql -u root food_supply_chain < database/schema.sql
mysql -u root food_supply_chain < database/sample_data.sql
```

### Step 3: Configure Database Credentials
Open `src/main/resources/application.properties` and verify your MySQL connection settings:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_supply_chain?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
spring.datasource.username=root
spring.datasource.password=
```
*(If your local MySQL root account requires a password, enter it after `spring.datasource.password=`)*

### Step 4: Build and Run the Application
Execute:
```bash
mvn spring-boot:run
```
Wait until you see the log line:
```
Started FoodFlowApplication in X.XXX seconds
```

### Step 5: Open FoodFlow in Your Browser
Open your browser and navigate to:
👉 **http://localhost:8080**

Because authentication is active, you will be redirected to the **Operator Sign In** page (`/login.html`).

1. Click **Create an account ↗** (or go to `http://localhost:8080/signup.html`).
2. Register your operator details (e.g., Name: `Ananya Mehta`, Email: `ananya@company.com`, Password: `Password123!`).
3. You will be redirected to the sign-in page. Log in with your email and password.
4. You will land on the **FoodFlow Overview Dashboard** with your user initials, name, and `OPERATOR` badge displayed in the top bar!

---

## 🧪 Running Automated Tests

To run the complete test suite (unit tests and Spring Security integration tests):
```bash
mvn test
```

### Test Coverage Highlights
- `AuthSecurityIntegrationTest`:
  - Verifies `/api/dashboard` and `/api/products` reject unauthenticated calls with HTTP 401.
  - Verifies public accessibility of `/login.html`, `/signup.html`, and `/styles.css`.
  - Verifies registration, BCrypt password hashing, default `ROLE_USER` assignment, duplicate email prevention, and full login session lifecycle.
- `UserTest`:
  - Validates encapsulation, field validation, and exclusion of password hashes in public DTOs.
  - Validates inheritance from `Person` and polymorphic `getRoleLabel()` behavior.
- `FoodProductTest`:
  - Validates stock threshold detection and category-dependent storage advice polymorphism.

---

## 🏛️ OOP Concepts Demonstrated (Viva Reference)

1. **Encapsulation**:
   - `User`, `InventoryItem`, `FoodProduct`, `Supplier`, and request DTOs declare private fields.
   - Access is mediated through validated getters and setters (e.g. quantity cannot be negative; password cannot be blank; email format checks).
   - `UserResponse` encapsulates user details and strictly prevents password hashes from leaking across API responses.

2. **Inheritance**:
   - `User extends Person`: Inherits common identity properties (`id`, `contactName`, `email`, `phone`).
   - `Supplier extends Person`: Shares human/contact traits while adding organizational attributes (`companyName`, `city`, `status`).
   - `FoodProduct extends InventoryItem`: Inherits stock-keeping logic (`sku`, `quantity`, `reorderLevel`, `unitPrice`).

3. **Abstraction**:
   - Abstract class `Person` declares the abstract method `public abstract String getRoleLabel()`. Subclasses must provide their own specific role representation.
   - Abstract class `InventoryItem` declares abstract methods `getCategoryLabel()` and `getStorageAdvice()`.

4. **Polymorphism (Dynamic Method Dispatch)**:
   - When calling `.getRoleLabel()` on a `Person` reference pointing to a `User`, it dynamically returns `"Supply Chain Operator"` (or `"System Administrator"`).
   - Calling `.getStorageAdvice()` on a `FoodProduct` returns category-specific guidelines (e.g., `"Keep refrigerated at 2–5°C"` for Dairy vs. `"Keep dry and sealed"` for Grains).
   - The viva helper `OopConceptDemo.describe(InventoryItem item)` demonstrates runtime dispatch without coupling to concrete classes.

5. **Interfaces**:
   - `Trackable`: Defines traceability contracts (`getTrackingLabel()`, `isLowStock()`).
   - `ProductOperations`: Decouples product service implementations from controllers.
   - `UserOperations`: Decouples user registration and lookup logic from security filters.
   - Spring Security's `UserDetailsService`: Standard security contract implemented by `UserServiceImpl`.

6. **Service / Repository Separation of Concerns**:
   - Controllers (`ProductController`, `SupplierController`, `OrderController`, `AuthController`) handle HTTP routing, DTO validation, and status codes.
   - Services (`ProductServiceImpl`, `UserServiceImpl`) encapsulate business rules, password hashing, and role policies.
   - Repositories (`ProductRepository`, `SupplierRepository`, `OrderRepository`, `UserRepository`) use `JdbcTemplate` with prepared SQL statements.

7. **Centralized Exception Handling**:
   - `GlobalExceptionHandler` leverages `@RestControllerAdvice` to map domain exceptions (`BadCredentialsException`, `ResourceNotFoundException`, `DataIntegrityViolationException`, `MethodArgumentNotValidException`) into consistent, structured JSON error responses.

---

## 📡 REST API Reference

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new operator (defaults to `ROLE_USER`) |
| POST | `/api/auth/login` | Public | Authenticate and establish session |
| GET | `/api/auth/me` | Public | Inspect current user session & role |
| GET | `/api/auth/csrf` | Public | Retrieve CSRF token metadata |
| POST | `/api/auth/logout` | Authenticated | Terminate session & clear cookies |
| GET | `/api/dashboard` | Authenticated | Summary statistics & low-stock counts |
| GET | `/api/products` | Authenticated | List all inventory catalogue items |
| POST | `/api/products` | Authenticated | Add a new inventory product |
| PUT | `/api/products/{id}` | Authenticated | Update an existing product |
| DELETE | `/api/products/{id}` | Authenticated | Remove an unused product |
| GET | `/api/products/low-stock` | Authenticated | Fetch items at or below reorder level |
| GET | `/api/suppliers` | Authenticated | List all active sourcing suppliers |
| POST | `/api/suppliers` | Authenticated | Register a new supplier |
| PUT | `/api/suppliers/{id}` | Authenticated | Update supplier contact details |
| DELETE | `/api/suppliers/{id}` | Authenticated | Delete a supplier (if not referenced) |
| GET | `/api/orders` | Authenticated | List all purchase orders |
| POST | `/api/orders` | Authenticated | Create a new purchase order |
| PATCH | `/api/orders/{id}/status` | Authenticated | Update order workflow status |

---

## 🗄️ Database Schema Summary

The MySQL database `food_supply_chain` consists of 5 relational tables:
- `users`: ID, full name, unique email, hashed password, phone, role (`ROLE_USER`), status (`ACTIVE`), creation timestamp.
- `products`: ID, SKU, product name, category, quantity, reorder level, unit price, expiry date, supplier foreign key.
- `suppliers`: ID, company name, contact person, unique email, phone, city, status (`ACTIVE` / `INACTIVE`).
- `orders`: ID, unique order code, supplier foreign key, order date, status, total amount, notes.
- `order_items`: ID, order foreign key, product foreign key, quantity, unit price, line total.

---

## 🔧 Troubleshooting Tips

- **`Communications link failure` on port 3306**:
  Ensure MySQL is active: `brew services list` or `brew services restart mysql`.
- **`Access denied for user 'root'@'localhost'`**:
  Set your local password in `src/main/resources/application.properties` at `spring.datasource.password=your_password`.
- **`Port 8080 already in use`**:
  Find the existing process: `lsof -i :8080`, stop it, or launch on another port via `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"`.
- **CSRF Token Mismatch**:
  The application automatically synchronizes `XSRF-TOKEN` via cookies and `/api/auth/csrf`. If clearing browser data, simply refresh the page.
