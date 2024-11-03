package com.webtech.rail.rail.userRepository;

import com.webtech.rail.rail.model.Schedule;
import com.webtech.rail.rail.model.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // Basic queries using method names
    Page<Schedule> findByStatus(ScheduleStatus status, Pageable pageable);
    List<Schedule> findByTrain_TrainId(Long trainId);
    List<Schedule> findByRouteId(Long routeId);
    List<Schedule> findByDepartureBetween(LocalDateTime start, LocalDateTime end);
    List<Schedule> findByTrain_TrainIdAndStatus(Long trainId, ScheduleStatus status);

    // Search queries - modified to use proper types
    Page<Schedule> findByTrain_TrainIdContaining(Long trainId, Pageable pageable);
    List<Schedule> findByTrain_TrainIdAndStatus(Long trainId, ScheduleStatus status, Pageable pageable);

    // Complex queries using @Query
    @Query("SELECT s FROM Schedule s WHERE s.train.trainId = :trainId " +
            "AND s.id != :scheduleId " +
            "AND s.status != 'CANCELLED' " +
            "AND ((s.departure BETWEEN :departure AND :arrival) " +
            "OR (s.arrival BETWEEN :departure AND :arrival))")
    List<Schedule> findOverlappingSchedules(
            @Param("trainId") Long trainId,
            @Param("scheduleId") Long scheduleId,
            @Param("departure") LocalDateTime departure,
            @Param("arrival") LocalDateTime arrival
    );

    @Query("SELECT s FROM Schedule s WHERE s.departure > :now AND s.status = 'ACTIVE' ORDER BY s.departure")
    List<Schedule> findUpcomingSchedules(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM Schedule s WHERE s.status = 'ACTIVE' " +
            "AND s.departure < :now AND s.arrival > :now " +
            "ORDER BY s.departure")
    List<Schedule> findDelayedSchedules(@Param("now") LocalDateTime now);
}