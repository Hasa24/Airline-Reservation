package com.airline;

import com.airline.dto.AuthDto;
import com.airline.dto.BookingDto;
import com.airline.dto.FlightDto;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.repository.FlightRepository;
import com.airline.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AirlineReservationTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired FlightRepository flightRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static String passengerToken;
    private static String adminToken;
    private static Long   flightId;
    private static String bookingRef;

    @BeforeEach
    void setup() {
        if (!userRepository.existsByEmail("test@example.com")) {
            userRepository.save(User.builder()
                .fullName("Test Passenger").email("test@example.com")
                .password(passwordEncoder.encode("pass123"))
                .phone("1234567890").role(User.Role.PASSENGER).build());
        }
        if (!userRepository.existsByEmail("admin@test.com")) {
            userRepository.save(User.builder()
                .fullName("Test Admin").email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("0000000000").role(User.Role.ADMIN).build());
        }
    }

    @Test @Order(1)
    void testPassengerLogin() throws Exception {
        var req = new AuthDto.LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

        passengerToken = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test @Order(2)
    void testAdminLogin() throws Exception {
        var req = new AuthDto.LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        adminToken = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("token").asText();
    }

    @Test @Order(3)
    void testAdminCreateFlight() throws Exception {
        var req = new FlightDto.CreateRequest();
        req.setFlightNumber("TEST001");
        req.setOrigin("New York");
        req.setDestination("London");
        req.setDepartureTime(LocalDateTime.now().plusDays(30));
        req.setArrivalTime(LocalDateTime.now().plusDays(30).plusHours(7));
        req.setAircraftType("Boeing 777");
        req.setTotalFirstClassSeats(4);
        req.setTotalBusinessSeats(8);
        req.setTotalEconomySeats(100);
        req.setFirstClassPrice(BigDecimal.valueOf(1200));
        req.setBusinessPrice(BigDecimal.valueOf(600));
        req.setEconomyPrice(BigDecimal.valueOf(250));

        MvcResult result = mockMvc.perform(post("/api/flights")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.flightNumber").value("TEST001"))
            .andReturn();

        flightId = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asLong();
    }

    @Test @Order(4)
    void testSearchFlights() throws Exception {
        mockMvc.perform(get("/api/flights/search")
                .param("origin", "New York")
                .param("destination", "London")
                .param("from", LocalDateTime.now().toString())
                .param("to", LocalDateTime.now().plusDays(60).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].flightNumber").value("TEST001"));
    }

    @Test @Order(5)
    void testBookSeat() throws Exception {
        var req = new BookingDto.CreateRequest();
        req.setFlightId(flightId);
        req.setSeatClass("ECONOMY");

        MvcResult result = mockMvc.perform(post("/api/bookings")
                .header("Authorization", "Bearer " + passengerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bookingReference").exists())
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andReturn();

        bookingRef = objectMapper.readTree(result.getResponse().getContentAsString())
            .get("bookingReference").asText();
    }

    @Test @Order(6)
    void testGetMyBookings() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                .header("Authorization", "Bearer " + passengerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test @Order(7)
    void testCancelBooking() throws Exception {
        mockMvc.perform(put("/api/bookings/" + bookingRef + "/cancel")
                .header("Authorization", "Bearer " + passengerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test @Order(8)
    void testPassengerCannotCreateFlight() throws Exception {
        var req = new FlightDto.CreateRequest();
        req.setFlightNumber("HACK001");

        mockMvc.perform(post("/api/flights")
                .header("Authorization", "Bearer " + passengerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }
}
