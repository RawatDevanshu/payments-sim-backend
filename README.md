# 💳 Payment Simulation Backend

A Spring Boot backend that simulates UPI/wallet payments.  
Features include JWT-based authentication, secure bank PIN validation, wallet top-ups, and transaction logging — designed to mirror real-world payment flows like PhonePe or GPay.

---

## 📑 Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Setup & Installation](#setup--installation)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Future Enhancements](#future-enhancements)

---

## 🚀 Features
- User registration & login with JWT
- Bank account creation with PIN (hashed via BCrypt)
- Wallet creation with UPI handle
- Top-up wallet from linked bank account
- Wallet-to-wallet transactions with wallet PIN
- Global exception handling with clean API responses
- Swagger/OpenAPI documentation

---

## 🛠 Tech Stack
- **Backend:** Spring Boot 3, Spring Security 6, JPA/Hibernate  
- **Database:** PostgreSQL  
- **Migration:** Flyway  
- **Auth:** JWT (jjwt library)  
- **Validation:** Spring Validation  
- **Docs:** Springdoc OpenAPI (Swagger UI)

---

## 🏗 Architecture

src/main/java/com/example/paymentsimulation \
├── config/        # Security & JWT configs \
├── controller/    # REST endpoints \
├── dto/           # Request/response DTOs \
├── entity/        # JPA entities \
├── repository/    # Data access layer \
├── service/       # Business logic \
├── exception/     # Global error handling \
└── util/          # Helpers (ApiResponse, etc.)

---

## ⚙️ Setup & Installation

1. **Clone the repo**
   ```bash
   git clone https://github.com/yourname/payment-sim.git
   cd payment-sim


- Configure environment variables in .env
DB_URL=jdbc:postgresql://localhost:5432/paymentsim
DB_USER=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your-secret-key
- Run migrations
mvn flyway:migrate


- Start the app
mvn spring-boot:run



📡 API Endpoints
Auth
- POST /api/auth/register → Register new user
- POST /api/auth/login → Login & get JWT
Bank Accounts
- POST /api/bank-accounts → Create & link bank account (requires JWT)
Wallets
- POST /api/wallets → Create wallet with UPI handle
- POST /api/wallets/topup → Transfer money from bank to wallet (requires bank PIN)
Transactions
- POST /api/transactions → Wallet-to-wallet transfer (requires wallet PIN)

🔐 Security
- JWT for authentication
- Role-based authorization with Spring Security
- BCrypt-hashed PINs for sensitive operations
- Rate limiting for PIN attempts (planned)
- Global exception handling for consistent error codes

🌱 Future Enhancements
- Fraud detection rules
- Scheduled payments
- Analytics dashboard
- Multi-bank linking
- Rate limiting middleware

🎯 Why This Project?
This project demonstrates real-world backend engineering:
- Secure authentication with JWT
- Layered security using PINs
- Clean architecture with DTOs, services, and repositories
- Database migrations and validation
- Production-grade error handling
It’s designed to showcase backend skills in Java, Spring Boot, and security best practices, making it a strong portfolio piece for interviews and team adoption.

---