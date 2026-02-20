package com.airline.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private String aircraftType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    // Seat counts
    @Column(nullable = false)
    private int totalFirstClassSeats;

    @Column(nullable = false)
    private int totalBusinessSeats;

    @Column(nullable = false)
    private int totalEconomySeats;

    @Column(nullable = false)
    private int availableFirstClassSeats;

    @Column(nullable = false)
    private int availableBusinessSeats;

    @Column(nullable = false)
    private int availableEconomySeats;

    // Base prices per class
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal firstClassPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal businessPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal economyPrice;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    public enum FlightStatus { SCHEDULED, BOARDING, DEPARTED, ARRIVED, CANCELLED, DELAYED }

    public boolean hasAvailableSeats(String seatClass) {
        return switch (seatClass.toUpperCase()) {
            case "FIRST"    -> availableFirstClassSeats > 0;
            case "BUSINESS" -> availableBusinessSeats > 0;
            case "ECONOMY"  -> availableEconomySeats > 0;
            default -> false;
        };
    }

    public BigDecimal getPriceForClass(String seatClass) {
        return switch (seatClass.toUpperCase()) {
            case "FIRST"    -> firstClassPrice;
            case "BUSINESS" -> businessPrice;
            case "ECONOMY"  -> economyPrice;
            default -> throw new IllegalArgumentException("Invalid seat class: " + seatClass);
        };
    }

    public void decrementSeat(String seatClass) {
        switch (seatClass.toUpperCase()) {
            case "FIRST"    -> availableFirstClassSeats--;
            case "BUSINESS" -> availableBusinessSeats--;
            case "ECONOMY"  -> availableEconomySeats--;
        }
    }

    public void incrementSeat(String seatClass) {
        switch (seatClass.toUpperCase()) {
            case "FIRST"    -> availableFirstClassSeats++;
            case "BUSINESS" -> availableBusinessSeats++;
            case "ECONOMY"  -> availableEconomySeats++;
        }
    }
}
