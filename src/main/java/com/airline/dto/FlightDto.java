package com.airline.dto;

import com.airline.entity.Flight;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FlightDto {

    @Data
    public static class CreateRequest {
        @NotBlank
        private String flightNumber;

        @NotBlank
        private String origin;

        @NotBlank
        private String destination;

        @NotNull
        @Future(message = "Departure must be in the future")
        private LocalDateTime departureTime;

        @NotNull
        @Future(message = "Arrival must be in the future")
        private LocalDateTime arrivalTime;

        @NotBlank
        private String aircraftType;

        @Min(1) private int totalFirstClassSeats;
        @Min(1) private int totalBusinessSeats;
        @Min(1) private int totalEconomySeats;

        @NotNull @DecimalMin("0.01") private BigDecimal firstClassPrice;
        @NotNull @DecimalMin("0.01") private BigDecimal businessPrice;
        @NotNull @DecimalMin("0.01") private BigDecimal economyPrice;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String flightNumber;
        private String origin;
        private String destination;
        private LocalDateTime departureTime;
        private LocalDateTime arrivalTime;
        private String aircraftType;
        private String status;

        private int availableFirstClassSeats;
        private int availableBusinessSeats;
        private int availableEconomySeats;

        private BigDecimal firstClassPrice;
        private BigDecimal businessPrice;
        private BigDecimal economyPrice;

        public static Response from(Flight f) {
            return Response.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .origin(f.getOrigin())
                .destination(f.getDestination())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .aircraftType(f.getAircraftType())
                .status(f.getStatus().name())
                .availableFirstClassSeats(f.getAvailableFirstClassSeats())
                .availableBusinessSeats(f.getAvailableBusinessSeats())
                .availableEconomySeats(f.getAvailableEconomySeats())
                .firstClassPrice(f.getFirstClassPrice())
                .businessPrice(f.getBusinessPrice())
                .economyPrice(f.getEconomyPrice())
                .build();
        }
    }

    @Data
    public static class SearchRequest {
        @NotBlank private String origin;
        @NotBlank private String destination;
        @NotNull  private LocalDateTime from;
        @NotNull  private LocalDateTime to;
    }

    @Data
    public static class StatusUpdateRequest {
        @NotBlank private String status;
    }
}
