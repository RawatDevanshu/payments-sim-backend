# 💳 Real-Time Payment Simulation Engine

A production-ready Spring Boot backend engine simulating UPI and digital wallet payment systems (e.g., PhonePe, GPay). Built to showcase enterprise backend patterns: **concurrency-safe balance transfers, idempotency guarantees, staged state processing, and API rate-limiting**.

---

## 🛠️ System Design Decisions (What Makes This Project Unique)

### 1. Concurrency Control & Deadlock Prevention
*   **Challenge**: When two users simultaneously transfer money to each other, a circular dependency of database locks can occur (User A locks Wallet A and waits for Wallet B; User B locks Wallet B and waits for Wallet A), leading to database **deadlocks**.
*   **Solution**: Implemented a **lexicographical lock-ordering algorithm** on UPI handles. Wallets are locked in a deterministic alphanumeric order (using pessimistic write locks `SELECT FOR UPDATE`). This guarantees that concurrent transfers between the same users always acquire locks in the exact same sequence, preventing deadlocks.
*   **Validation**: Tested via multi-threaded JUnit integration tests using `CountDownLatch` and `ExecutorService` to verify balance integrity and double-spend protection.

### 2. Idempotency Guarantees
*   **Challenge**: Network instability can cause clients to retry a payment request. Without idempotency, this results in double-charging the user.
*   **Solution**: Created a custom, database-backed idempotency filter. Requests containing an `Idempotency-Key` header are tracked:
    *   `PROCESSING`: Subsequent duplicate requests are rejected to prevent race conditions.
    *   `SUCCESS` / `FAILED`: The server bypasses execution and immediately returns the cached API response from the database.

### 3. Staged Transaction Lifecycle (State Machine Pattern)
*   To ensure auditability, payments are not updated in a single step. Transactions transition through distinct database states:
    `CREATED ──> VALIDATING ──> PROCESSING ──> DEBIT_PENDING ──> CREDIT_PENDING ──> COMPLETED/FAILED`
    Any unexpected validation or business logic exception (e.g., PIN not set, invalid PIN, or database errors) is caught by a global transaction handler, updating the record status to `FAILED` with details, and executing a clean rollback of the balance changes.

### 4. API Rate Limiting
*   Integrated **Bucket4j** to throttle API requests at the servlet filter level. Requests are rate-limited based on JWT User Principal or Fallback Client IP (using token bucket algorithm), preventing brute-force attacks on PINs and auth endpoints.

---

## 💻 Tech Stack
*   **Core Framework**: Spring Boot 3, Spring Security 6 (JWT stateless authentication)
*   **Database**: PostgreSQL (Development/Production), H2 (In-memory test scope)
*   **Migrations**: Flyway Schema Migration
*   **Testing**: JUnit 5, Spring Boot Test (Concurrency Stress Tests)
*   **Containerization**: Docker & Docker Compose

---

## 🔌 API Architecture

### Authentication
*   `POST /api/auth/register` - Create user profile
*   `POST /api/auth/login` - Authenticate user (issues HTTP-Only Secure JWT Cookie)

### Wallets & Banking
*   `POST /api/bank-accounts/open` - Link a new bank account (defaults with seed balance)
*   `POST /api/wallets/create` - Provision a wallet linked to a unique UPI handle
*   `POST /api/pins/set` - Configure secure hashed PIN (BCrypt)

### Transaction Processing
*   `POST /api/wallets/topup` - Transfer funds from Bank Account to Wallet (requires Bank PIN)
*   `POST /api/wallets/withdraw` - Transfer funds from Wallet to Bank Account (requires UPI PIN)
*   `POST /api/transactions/transfer` - Wallet-to-wallet transfer (requires UPI PIN + `Idempotency-Key` header)
*   `GET /api/transactions/wallet/{upiHandle}` - Paginated audit log of transaction history

---

## 🚀 Local Development Setup

1.  **Clone the Repo**:
    ```bash
    git clone https://github.com/your-username/payment-sim-backend.git
    cd payment-sim-backend
    ```
2.  **Spin up Postgres Container**:
    ```bash
    docker-compose up -d db
    ```
3.  **Run the Server**:
    ```bash
    ./mvnw spring-boot:run
    ```
4.  **Execute Integration Tests**:
    ```bash
    ./mvnw test
    ```