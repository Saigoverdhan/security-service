# 🔐 Security Microservice

A production-style **Spring Boot Security Microservice** built using **Java 21**, **Spring Boot 3**, **Spring Security 6**, **JWT Authentication**, **Refresh Tokens**, **Email OTP Verification**, and **Google OAuth2 Login**.

This microservice is designed as the authentication service for a scalable microservices-based online learning platform.

---

# 🚀 Features

- ✅ User Registration
- ✅ Email OTP Verification
- ✅ Username & Email Validation
- ✅ BCrypt Password Encryption
- ✅ JWT Authentication
- ✅ Refresh Token Authentication
- ✅ Forgot Password
- ✅ Reset Password
- ✅ Google OAuth2 Login
- ✅ Local + Google Authentication Support
- ✅ Role Based Authentication
- ✅ Global Exception Handling
- ✅ SLF4J Logging
- ✅ Transaction Management
- ✅ REST APIs
- ✅ MySQL Database

---

# 🛠 Tech Stack

- Java 21
- Spring Boot 3
- Spring Security 6
- Spring Data JPA
- Hibernate
- MySQL
- JWT
- OAuth2 Client
- Spring Mail
- Lombok
- Maven
- SLF4J Logging

---

# 📂 Project Structure

```
src
 ├── controller
 ├── dto
 │     ├── request
 │     └── response
 ├── entity
 ├── enums
 ├── exception
 ├── repository
 ├── security
 │     ├── config
 │     ├── filter
 │     ├── jwt
 │     └── oauth2
 ├── service
 └── MicroserviceApplication
```

---

# 🔑 Authentication Flow

## Local Registration

```
User Registration
        │
        ▼
Validate Username & Email
        │
        ▼
Encrypt Password
        │
        ▼
Save User
        │
        ▼
Generate OTP
        │
        ▼
Send OTP Email
```

---

## Email Verification

```
User
   │
   ▼
Enter OTP
   │
   ▼
Verify OTP
   │
   ▼
Enable Account
```

---

## Login Flow

```
Username + Password
          │
          ▼
Authentication Manager
          │
          ▼
JWT Access Token
+
Refresh Token
```

---

## JWT Authentication

```
Client
   │
Authorization: Bearer Token
   │
   ▼
JwtAuthenticationFilter
   │
   ▼
Validate Token
   │
   ▼
Protected API
```

---

## Refresh Token Flow

```
Access Token Expired
          │
          ▼
POST /auth/refresh-token
          │
          ▼
Validate Refresh Token
          │
          ▼
Generate New Access Token
```

---

## Google OAuth2 Login

```
Google Login
      │
      ▼
Google Authentication
      │
      ▼
Existing User?
      │
 ┌────┴─────┐
 │          │
Yes        No
 │          │
 ▼          ▼
Login   Create Account
 │          │
 └────┬─────┘
      ▼
Generate JWT
```

---

# 📌 REST APIs

## Authentication

| Method | Endpoint |
|---------|----------|
| POST | /auth/register |
| POST | /auth/verify-otp |
| POST | /auth/login |
| POST | /auth/refresh-token |
| POST | /auth/forgot-password |
| POST | /auth/reset-password |

---

## OAuth2

```
GET /oauth2/authorization/google
```

---

# 🗄 Database Tables

### users

- id
- username
- email
- password
- role
- provider
- enabled
- email_verified

---

### otp

- id
- otp
- expiry_time
- user_id

---

### refresh_tokens

- id
- token
- expiry_date
- user_id

---

# 🔐 Security Features

- BCrypt Password Encryption
- Stateless JWT Authentication
- Refresh Token Authentication
- Email Verification
- OTP Expiration
- Role Based Authorization
- Google OAuth2 Authentication
- Global Exception Handling
- Transaction Rollback
- Secure Logging

---

# 📖 Logging

Logs are available in:

```
logs/security-microservice.log
```

Logging includes:

- User Registration
- Login Attempts
- OTP Generation
- Password Reset
- JWT Authentication
- Refresh Token
- Google Login
- Errors & Exceptions

---

# ⚙ Configuration

Configure the following in `application.properties`.

- MySQL
- Gmail SMTP
- JWT Secret
- OAuth2 Client ID
- OAuth2 Client Secret

---

# ▶️ Run the Project

Clone the repository

```bash
git clone <repository-url>
```

Move into the project

```bash
cd security-microservice
```

Build the project

```bash
mvn clean install
```

Run

```bash
mvn spring-boot:run
```

---

# 📈 Future Improvements

- Logout API
- Refresh Token Rotation
- Multi-device Session Management
- Swagger / OpenAPI Documentation
- Docker Support
- Kubernetes Deployment
- Redis Token Blacklist
- Rate Limiting
- Audit Logging

---

# 👨‍💻 Author

**Sai Goverdhan**

Backend Developer

Spring Boot | Microservices | Java | Spring Security | JWT | OAuth2 | Docker | AWS

---

# ⭐ Project Status

✅ Completed

This Security Microservice is production-style and serves as the authentication service for a microservices-based learning platform.
