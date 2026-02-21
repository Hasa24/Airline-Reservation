# ✈️ Airline Reservation System

A production-ready REST API built with **Java 17**, **Spring Boot 3**, and **JWT Authentication**.

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



