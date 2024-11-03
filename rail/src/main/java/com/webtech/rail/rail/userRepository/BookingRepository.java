package com.webtech.rail.rail.userRepository;


import com.webtech.rail.rail.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Changed method names to match the bookingStatus field name
    Page<Booking> findByBookingStatus(String bookingStatus, Pageable pageable);

    Page<Booking> findByUser_EmailContainingIgnoreCase(String search, Pageable pageable);

    Page<Booking> findByUser_EmailContainingIgnoreCaseAndBookingStatus(
            String search,
            String bookingStatus,
            Pageable pageable
    );

    List<Booking> findByBookingStatus(String bookingStatus);

    List<Booking> findByUser_EmailContainingIgnoreCase(String search);

    List<Booking> findByUser_EmailContainingIgnoreCaseAndBookingStatus(
            String search,
            String bookingStatus
    );

    Page<Booking> findByPassengerContainingIgnoreCaseOrTrain_NameContainingIgnoreCase(
            String passenger, String trainName, Pageable pageable);

    // Add method for user's bookings
    List<Booking> findByUser_IdAndBookingStatus(Long userId, String bookingStatus);
}
