package com.airline.service;

import com.airline.dto.BookingDto;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.exception.BadRequestException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.repository.BookingRepository;
import com.airline.repository.FlightRepository;
import com.airline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final FlightRepository  flightRepository;
    private final UserRepository    userRepository;

    // ── Book a Seat ────────────────────────────────────────────────────────

    @Transactional
    public BookingDto.Response bookSeat(String passengerEmail, BookingDto.CreateRequest request) {
        User passenger = userRepository.findByEmail(passengerEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + passengerEmail));

        // PESSIMISTIC LOCK — prevents two users booking the last seat simultaneously
        Flight flight = flightRepository.findByIdWithLock(request.getFlightId())
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + request.getFlightId()));

        if (flight.getStatus() != Flight.FlightStatus.SCHEDULED &&
            flight.getStatus() != Flight.FlightStatus.BOARDING) {
            throw new BadRequestException("Cannot book a seat on a " + flight.getStatus() + " flight");
        }

        String seatClass = request.getSeatClass().toUpperCase();

        if (!flight.hasAvailableSeats(seatClass)) {
            throw new BadRequestException("No " + seatClass + " seats available on flight " + flight.getFlightNumber());
        }

        // Decrement seat count and assign seat number
        flight.decrementSeat(seatClass);
        String seatNumber = generateSeatNumber(seatClass, flight);
        flightRepository.save(flight);

        Booking booking = Booking.builder()
            .bookingReference(generateReference())
            .passenger(passenger)
            .flight(flight)
            .seatClass(seatClass)
            .seatNumber(seatNumber)
            .totalPrice(flight.getPriceForClass(seatClass))
            .status(Booking.BookingStatus.CONFIRMED)
            .bookedAt(LocalDateTime.now())
            .build();

        return BookingDto.Response.from(bookingRepository.save(booking));
    }

    // ── Cancel a Booking ───────────────────────────────────────────────────

    @Transactional
    public BookingDto.Response cancelBooking(String bookingReference, String requesterEmail) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingReference));

        // Only the passenger or an admin can cancel
        boolean isOwner = booking.getPassenger().getEmail().equals(requesterEmail);
        boolean isAdmin  = userRepository.findByEmail(requesterEmail)
            .map(u -> u.getRole() == User.Role.ADMIN).orElse(false);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        // Free up the seat (with lock)
        Flight flight = flightRepository.findByIdWithLock(booking.getFlight().getId())
            .orElseThrow();
        flight.incrementSeat(booking.getSeatClass());
        flightRepository.save(flight);

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        return BookingDto.Response.from(bookingRepository.save(booking));
    }

    // ── Get My Bookings ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookingDto.Response> getMyBookings(String email) {
        User passenger = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return bookingRepository.findByPassengerIdOrderByBookedAtDesc(passenger.getId())
            .stream()
            .map(BookingDto.Response::from)
            .toList();
    }

    // ── Get Booking by Reference ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public BookingDto.Response getBooking(String bookingReference, String requesterEmail) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingReference));

        boolean isOwner = booking.getPassenger().getEmail().equals(requesterEmail);
        boolean isAdmin  = userRepository.findByEmail(requesterEmail)
            .map(u -> u.getRole() == User.Role.ADMIN).orElse(false);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this booking");
        }

        return BookingDto.Response.from(booking);
    }

    // ── Admin: All bookings for a flight ──────────────────────────────────

    @Transactional(readOnly = true)
    public List<BookingDto.Response> getFlightBookings(Long flightId) {
        return bookingRepository.findConfirmedByFlightId(flightId)
            .stream()
            .map(BookingDto.Response::from)
            .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private String generateReference() {
        return "CA-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateSeatNumber(String seatClass, Flight flight) {
        String prefix = switch (seatClass) {
            case "FIRST"    -> "F";
            case "BUSINESS" -> "B";
            default         -> "E";
        };
        int row = new Random().nextInt(30) + 1;
        char col = (char) ('A' + new Random().nextInt(6));
        return prefix + row + col;
    }
}
