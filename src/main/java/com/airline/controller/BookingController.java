package com.airline.controller;

import com.airline.dto.BookingDto;
import com.airline.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Seat booking and reservation management")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Book a seat on a flight")
    public ResponseEntity<BookingDto.Response> bookSeat(
            @Valid @RequestBody BookingDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bookingService.bookSeat(currentUser.getUsername(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all my bookings")
    public ResponseEntity<List<BookingDto.Response>> getMyBookings(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser.getUsername()));
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "Get booking by reference number")
    public ResponseEntity<BookingDto.Response> getBooking(
            @PathVariable String bookingReference,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(bookingService.getBooking(bookingReference, currentUser.getUsername()));
    }

    @PutMapping("/{bookingReference}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<BookingDto.Response> cancelBooking(
            @PathVariable String bookingReference,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingReference, currentUser.getUsername()));
    }

    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all bookings for a flight (Admin only)")
    public ResponseEntity<List<BookingDto.Response>> getFlightBookings(@PathVariable Long flightId) {
        return ResponseEntity.ok(bookingService.getFlightBookings(flightId));
    }
}
