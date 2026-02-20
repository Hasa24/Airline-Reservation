package com.airline.dto;

import com.airline.entity.Booking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingDto {

    @Data
    public static class CreateRequest {
        @NotNull(message = "Flight ID is required")
        private Long flightId;

        @NotBlank(message = "Seat class is required")
        @Pattern(regexp = "ECONOMY|BUSINESS|FIRST", message = "Seat class must be ECONOMY, BUSINESS, or FIRST")
        private String seatClass;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String bookingReference;
        private String passengerName;
        private String passengerEmail;
        private String flightNumber;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String seatClass;
        private String seatNumber;
        private BigDecimal totalPrice;
        private String status;
        private LocalDateTime bookedAt;

        public static Response from(Booking b) {
            return Response.builder()
                .id(b.getId())
                .bookingReference(b.getBookingReference())
                .passengerName(b.getPassenger().getFullName())
                .passengerEmail(b.getPassenger().getEmail())
                .flightNumber(b.getFlight().getFlightNumber())
                .origin(b.getFlight().getOrigin())
                .destination(b.getFlight().getDestination())
                .departureTime(b.getFlight().getDepartureTime())
                .arrivalTime(b.getFlight().getArrivalTime())
                .seatClass(b.getSeatClass())
                .seatNumber(b.getSeatNumber())
                .totalPrice(b.getTotalPrice())
                .status(b.getStatus().name())
                .bookedAt(b.getBookedAt())
                .build();
        }
    }
}
