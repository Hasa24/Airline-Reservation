package com.airline.repository;

import com.airline.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByPassengerIdOrderByBookedAtDesc(Long passengerId);

    @Query("""
        SELECT b FROM Booking b
        WHERE b.flight.id = :flightId
          AND b.status = 'CONFIRMED'
        ORDER BY b.seatNumber ASC
    """)
    List<Booking> findConfirmedByFlightId(@Param("flightId") Long flightId);

    boolean existsByFlightIdAndSeatNumberAndStatus(Long flightId, String seatNumber, Booking.BookingStatus status);
}
