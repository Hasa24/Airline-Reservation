# ✈️ Airline Reservation System

A production-ready REST API built with **Java 17**, **Spring Boot 3**, **PostgreSQL**, and **JWT Authentication**.

---

## 🚀 Quick Start

### Option A — Docker (Recommended)
```bash
git clone https://github.com/YOUR_USERNAME/airline-reservation
cd airline-reservation
docker-compose up --build
```
API is live at: http://localhost:8080

### Option B — Local (requires Java 17 + PostgreSQL)
```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE airline_db;"

# 2. Update credentials in:
#    src/main/resources/application.properties

# 3. Run
./mvnw spring-boot:run
```

---

## 📖 API Documentation

Swagger UI → http://localhost:8080/swagger-ui.html

---

## 🔑 Default Credentials (seeded on startup)

| Role      | Email                 | Password   |
|-----------|-----------------------|------------|
| Admin     | admin@cloudair.com    | admin123   |
| Passenger | Register via API      | —          |

---

## 🛠 Tech Stack

| Layer         | Technology                    |
|---------------|-------------------------------|
| Framework     | Spring Boot 3.2               |
| Language      | Java 17                       |
| Security      | Spring Security + JWT (JJWT)  |
| Database      | PostgreSQL + Spring Data JPA  |
| Documentation | Swagger / SpringDoc OpenAPI   |
| Build         | Maven                         |
| Container     | Docker + Docker Compose       |

---

## 📡 API Endpoints

### Auth
| Method | Endpoint             | Description          | Auth     |
|--------|----------------------|----------------------|----------|
| POST   | /api/auth/register   | Register passenger   | Public   |
| POST   | /api/auth/login      | Login, get JWT       | Public   |

### Flights
| Method | Endpoint                    | Description             | Auth     |
|--------|-----------------------------|-------------------------|----------|
| GET    | /api/flights                | All scheduled flights   | Public   |
| GET    | /api/flights/search         | Search by route & date  | Public   |
| GET    | /api/flights/{flightNumber} | Get flight details      | Public   |
| POST   | /api/flights                | Create flight           | Admin    |
| PUT    | /api/flights/{id}/status    | Update flight status    | Admin    |
| DELETE | /api/flights/{id}           | Delete flight           | Admin    |

### Bookings
| Method | Endpoint                          | Description              | Auth      |
|--------|-----------------------------------|--------------------------|-----------|
| POST   | /api/bookings                     | Book a seat              | Passenger |
| GET    | /api/bookings/my                  | My bookings              | Passenger |
| GET    | /api/bookings/{ref}               | Get booking by reference | Passenger |
| PUT    | /api/bookings/{ref}/cancel        | Cancel booking           | Passenger |
| GET    | /api/bookings/flight/{flightId}   | All bookings for flight  | Admin     |

---

## 🔒 Key Technical Decisions

### Preventing Double-Booking
Uses **PostgreSQL pessimistic write locking** to ensure two users cannot book the last available seat simultaneously:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT f FROM Flight f WHERE f.id = :id")
Optional<Flight> findByIdWithLock(@Param("id") Long id);
```

### Role-Based Access Control
- `PASSENGER` — can register, search flights, book and cancel own reservations
- `ADMIN` — can manage flights and view all bookings

### JWT Authentication
Stateless authentication with 24-hour token expiry. Pass the token as:
```
Authorization: Bearer <token>
```

---

## 🧪 Running Tests
```bash
./mvnw test
```
Tests use H2 in-memory database — no PostgreSQL required for testing.

---

## ☁️ Deployment (Railway)

1. Push code to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add a PostgreSQL plugin
4. Set environment variables:
   - `SPRING_DATASOURCE_URL` → from Railway PostgreSQL
   - `JWT_SECRET` → any 32+ character string
5. Deploy ✅

---

## 📁 Project Structure

```
src/main/java/com/airline/
├── AirlineReservationApplication.java
├── config/
│   ├── DataSeeder.java          # Seeds admin + sample flights
│   ├── OpenApiConfig.java       # Swagger config
│   └── SecurityConfig.java      # JWT + Spring Security
├── controller/
│   ├── AuthController.java
│   ├── FlightController.java
│   └── BookingController.java
├── dto/
│   ├── AuthDto.java
│   ├── FlightDto.java
│   └── BookingDto.java
├── entity/
│   ├── User.java
│   ├── Flight.java
│   └── Booking.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── ConflictException.java
│   └── BadRequestException.java
├── repository/
│   ├── UserRepository.java
│   ├── FlightRepository.java
│   └── BookingRepository.java
├── security/
│   ├── JwtUtil.java
│   └── JwtAuthFilter.java
└── service/
    ├── AuthService.java
    ├── FlightService.java
    └── BookingService.java
```
