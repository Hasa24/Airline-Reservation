package com.airline.repository;

import com.airline.entity.Flight;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    boolean existsByFlightNumber(String flightNumber);

    // Pessimistic write lock to prevent double-booking race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Flight f WHERE f.id = :id")
    Optional<Flight> findByIdWithLock(@Param("id") Long id);

    @Query("""
        SELECT f FROM Flight f
        WHERE LOWER(f.origin) = LOWER(:origin)
          AND LOWER(f.destination) = LOWER(:destination)
          AND f.departureTime >= :from
          AND f.departureTime <= :to
          AND f.status = 'SCHEDULED'
        ORDER BY f.departureTime ASC
    """)
    List<Flight> searchFlights(
        @Param("origin")      String origin,
        @Param("destination") String destination,
        @Param("from")        LocalDateTime from,
        @Param("to")          LocalDateTime to
    );

    @Query("""
        SELECT f FROM Flight f
        WHERE f.status = 'SCHEDULED'
        ORDER BY f.departureTime ASC
    """)
    List<Flight> findAllScheduled();
}
