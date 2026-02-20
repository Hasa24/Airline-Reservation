package com.airline.controller;

import com.airline.dto.FlightDto;
import com.airline.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "Flight management and search")
public class FlightController {

    private final FlightService flightService;

    // ── Public ─────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all scheduled flights")
    public ResponseEntity<List<FlightDto.Response>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllScheduled());
    }

    @GetMapping("/{flightNumber}")
    @Operation(summary = "Get flight by flight number")
    public ResponseEntity<FlightDto.Response> getFlight(@PathVariable String flightNumber) {
        return ResponseEntity.ok(flightService.getByFlightNumber(flightNumber));
    }

    @GetMapping("/search")
    @Operation(summary = "Search flights by origin, destination, and date range")
    public ResponseEntity<List<FlightDto.Response>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(flightService.searchFlights(origin, destination, from, to));
    }

    // ── Admin Only ─────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new flight (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<FlightDto.Response> createFlight(@Valid @RequestBody FlightDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlight(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update flight status (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<FlightDto.Response> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(flightService.updateStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a flight (Admin only)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }
}
