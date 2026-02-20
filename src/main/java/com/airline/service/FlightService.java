package com.airline.service;

import com.airline.dto.FlightDto;
import com.airline.entity.Flight;
import com.airline.exception.BadRequestException;
import com.airline.exception.ConflictException;
import com.airline.exception.ResourceNotFoundException;
import com.airline.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

    // ── Admin: Create Flight ───────────────────────────────────────────────

    @Transactional
    public FlightDto.Response createFlight(FlightDto.CreateRequest req) {
        if (flightRepository.existsByFlightNumber(req.getFlightNumber())) {
            throw new ConflictException("Flight number already exists: " + req.getFlightNumber());
        }
        if (!req.getArrivalTime().isAfter(req.getDepartureTime())) {
            throw new BadRequestException("Arrival time must be after departure time");
        }

        Flight flight = Flight.builder()
            .flightNumber(req.getFlightNumber())
            .origin(req.getOrigin())
            .destination(req.getDestination())
            .departureTime(req.getDepartureTime())
            .arrivalTime(req.getArrivalTime())
            .aircraftType(req.getAircraftType())
            .status(Flight.FlightStatus.SCHEDULED)
            .totalFirstClassSeats(req.getTotalFirstClassSeats())
            .totalBusinessSeats(req.getTotalBusinessSeats())
            .totalEconomySeats(req.getTotalEconomySeats())
            .availableFirstClassSeats(req.getTotalFirstClassSeats())
            .availableBusinessSeats(req.getTotalBusinessSeats())
            .availableEconomySeats(req.getTotalEconomySeats())
            .firstClassPrice(req.getFirstClassPrice())
            .businessPrice(req.getBusinessPrice())
            .economyPrice(req.getEconomyPrice())
            .build();

        return FlightDto.Response.from(flightRepository.save(flight));
    }

    // ── Public: Search & Browse ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FlightDto.Response> searchFlights(String origin, String destination,
                                                   LocalDateTime from, LocalDateTime to) {
        return flightRepository.searchFlights(origin, destination, from, to)
            .stream()
            .map(FlightDto.Response::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FlightDto.Response> getAllScheduled() {
        return flightRepository.findAllScheduled()
            .stream()
            .map(FlightDto.Response::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public FlightDto.Response getByFlightNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
            .map(FlightDto.Response::from)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + flightNumber));
    }

    // ── Admin: Update Status ───────────────────────────────────────────────

    @Transactional
    public FlightDto.Response updateStatus(Long id, String status) {
        Flight flight = flightRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));

        try {
            flight.setStatus(Flight.FlightStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        return FlightDto.Response.from(flightRepository.save(flight));
    }

    // ── Admin: Delete Flight ───────────────────────────────────────────────

    @Transactional
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new ResourceNotFoundException("Flight not found: " + id);
        }
        flightRepository.deleteById(id);
    }

    // ── Internal use by BookingService ─────────────────────────────────────

    @Transactional
    public Flight getFlightWithLock(Long id) {
        return flightRepository.findByIdWithLock(id)
            .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
    }
}
