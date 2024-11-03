package com.webtech.rail.rail.service;

import com.webtech.rail.rail.dto.DashboardStats;
import com.webtech.rail.rail.model.Schedule;
import com.webtech.rail.rail.model.ScheduleStatus;
import com.webtech.rail.rail.model.User;
import com.webtech.rail.rail.userRepository.AdminRepository;
import com.webtech.rail.rail.userRepository.BookingRepository;
import com.webtech.rail.rail.userRepository.ScheduleRepository;
import com.webtech.rail.rail.userRepository.TrainRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class DashboardService {
    @Autowired
    private TrainRepository trainRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private AdminRepository adminRepository;

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.setActiveTrainsCount(trainRepository.countByStatusIgnoreCase("active"));
        stats.setTodayBookingsCount(getTodayBookingsCount());
        Pageable unpaged = Pageable.unpaged();
        Page<Schedule> delayedSchedules = scheduleRepository.findByStatus(ScheduleStatus.DELAYED, unpaged);
        stats.setDelayedTrainsCount(delayedSchedules.getTotalElements());
        // Count delayed trains using Schedule status
        long delayedCount = scheduleRepository.findByStatus(ScheduleStatus.DELAYED, Pageable.unpaged())
                .getContent()
                .stream()
                .map(schedule -> schedule.getTrain().getTrainId()) // Get unique train IDs
                .distinct() // Remove duplicates
                .count();
        stats.setDelayedTrainsCount(delayedCount);

        // Count active users (users who have logged in at least once)
        long activeUsers = adminRepository.findByLastLoginIsNotNull().size();
        stats.setActiveUsersCount(activeUsers);

        // Set other stats...
        stats.setActiveTrainsCount(trainRepository.countByStatusIgnoreCase("active"));
        stats.setTodayBookingsCount(getTodayBookingsCount());


        stats.setActiveUsersCount(getActiveUsersCount());
        return stats;
    }

    public Page<?> getTabData(String activeTab, String searchTerm, String filterValue, int page, int pageSize, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        switch (activeTab) {
            case "trains":
                if (StringUtils.hasText(searchTerm)) {
                    return new PageImpl<>(trainRepository.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(
                            searchTerm, searchTerm));
                } else if (StringUtils.hasText(filterValue)) {
                    return new PageImpl<>(trainRepository.findByStatusIgnoreCase(filterValue));
                }
                return trainRepository.findAll(pageable);

            case "schedules":
                if (StringUtils.hasText(searchTerm)) {
                    // Convert searchTerm to Long for train_TrainId
                    try {
                        Long trainId = Long.parseLong(searchTerm);
                        return new PageImpl<>(scheduleRepository.findByTrain_TrainId(trainId));
                    } catch (NumberFormatException e) {
                        return Page.empty();
                    }
                } else if (StringUtils.hasText(filterValue)) {
                    try {
                        ScheduleStatus status = ScheduleStatus.valueOf(filterValue.toUpperCase());
                        return scheduleRepository.findByStatus(status, pageable);
                    } catch (IllegalArgumentException e) {
                        return Page.empty();
                    }
                }
                return scheduleRepository.findAll(pageable);

            case "bookings":
                if (StringUtils.hasText(searchTerm)) {
                    return bookingRepository.findByPassengerContainingIgnoreCaseOrTrain_NameContainingIgnoreCase(
                            searchTerm, searchTerm, pageable);
                } else if (StringUtils.hasText(filterValue)) {
                    return new PageImpl<>(bookingRepository.findByUser_IdAndBookingStatus(userId, filterValue));
                }
                return bookingRepository.findAll(pageable);

            default:
                return Page.empty();
        }
    }

    @Transactional//(readOnly = true)
    public List<User> getSignedInUsers() {
        return adminRepository.findByLastLoginIsNotNull();
    }

    private long getTodayBookingsCount() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getBookingDate().toLocalDate().equals(today)) // Use getBookingDate() and convert to LocalDate
                .count();
    }
   // private final Logger logger;
    private long getActiveUsersCount() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
          //  return adminRepository.countByLastLoginAfter(thirtyDaysAgo);
        } catch (Exception e) {
           // logger.error("Error getting active users count: " + e.getMessage());
            return 0;
        }
        return 0;
    }

    public List<Schedule> searchSchedules(String scheduleId, Long trainId) {
        if (scheduleId != null && !scheduleId.isEmpty()) {
            // Use orElse with an empty list to convert Optional to List
            Optional<Schedule> schedule = scheduleRepository.findById(Long.parseLong(scheduleId));
            return schedule.map(List::of).orElse(Collections.emptyList());
        }
        if (trainId != null && trainId==0) {
            // Add this method to ScheduleRepository if not already present
            return scheduleRepository.findByTrain_TrainId(trainId);
        }
        return scheduleRepository.findAll();
    }
}