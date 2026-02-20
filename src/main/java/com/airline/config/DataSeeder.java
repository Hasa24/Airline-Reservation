package com.airline.config;

import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.repository.FlightRepository;
import com.airline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository    userRepository;
    private final FlightRepository  flightRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedFlights();
    }

    private void seedAdmin() {
        if (!userRepository.existsByEmail("admin@cloudair.com")) {
            User admin = User.builder()
                .fullName("Admin User")
                .email("admin@cloudair.com")
                .password(passwordEncoder.encode("admin123"))
                .phone("+1-000-000-0000")
                .role(User.Role.ADMIN)
                .build();
            userRepository.save(admin);
            log.info("✅ Admin user created: admin@cloudair.com / admin123");
        }
    }

    private void seedFlights() {
        if (flightRepository.count() == 0) {
            flightRepository.save(buildFlight("CA101", "New York (JFK)", "Los Angeles (LAX)",
                LocalDateTime.now().plusDays(7).withHour(8).withMinute(0),
                LocalDateTime.now().plusDays(7).withHour(11).withMinute(30),
                800, 400, 149, 4, 12, 150));

            flightRepository.save(buildFlight("CA202", "Los Angeles (LAX)", "Chicago (ORD)",
                LocalDateTime.now().plusDays(10).withHour(14).withMinute(0),
                LocalDateTime.now().plusDays(10).withHour(20).withMinute(0),
                900, 450, 169, 4, 12, 130));

            flightRepository.save(buildFlight("CA303", "New York (JFK)", "Miami (MIA)",
                LocalDateTime.now().plusDays(5).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(5).withHour(12).withMinute(30),
                700, 350, 119, 4, 8, 100));

            log.info("✅ Sample flights seeded.");
        }
    }

    private Flight buildFlight(String number, String origin, String dest,
                               LocalDateTime dep, LocalDateTime arr,
                               int firstPrice, int bizPrice, int econPrice,
                               int firstSeats, int bizSeats, int econSeats) {
        return Flight.builder()
            .flightNumber(number)
            .origin(origin)
            .destination(dest)
            .departureTime(dep)
            .arrivalTime(arr)
            .aircraftType("Boeing 737")
            .status(Flight.FlightStatus.SCHEDULED)
            .totalFirstClassSeats(firstSeats)
            .totalBusinessSeats(bizSeats)
            .totalEconomySeats(econSeats)
            .availableFirstClassSeats(firstSeats)
            .availableBusinessSeats(bizSeats)
            .availableEconomySeats(econSeats)
            .firstClassPrice(BigDecimal.valueOf(firstPrice))
            .businessPrice(BigDecimal.valueOf(bizPrice))
            .economyPrice(BigDecimal.valueOf(econPrice))
            .build();
    }
}
