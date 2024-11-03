package com.webtech.rail.rail.userRepository;

import com.webtech.rail.rail.model.Train;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {

    // Count active trains within a specified period
    @Query("SELECT COUNT(t) FROM Train t WHERE t.status = 'ACTIVE' " +
            "AND t.createdDate BETWEEN :startDate AND :endDate")
    long countActiveTrainsInPeriod(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);


    // Find active trains in a specified period by status
    @Query("SELECT t FROM Train t WHERE t.status = :status AND t.createdDate BETWEEN :startDate AND :endDate")
    List<Train> findActiveTrainsInPeriod(@Param("status") String status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // Count trains by status before a certain date
    @Query("SELECT COUNT(t) FROM Train t WHERE t.status = :status AND t.createdDate < :date")
    long countByStatusAndCreatedDateBefore(@Param("status") String status, @Param("date") LocalDateTime date);

    // Count trains by status (case-insensitive)
    int countByStatusIgnoreCase(String status);

    // Find trains by status with pagination support
    Page<Train> findByStatus(String status, Pageable pageable);

    // Find by name or type containing case-insensitive text
    List<Train> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(String name, String type);

    // Search by name (case-insensitive) with optional pagination and status filtering
    Page<Train> findByNameContainingIgnoreCase(String search, Pageable pageable);
    Page<Train> findByNameContainingIgnoreCaseAndStatus(String search, String status, Pageable pageable);

    // Basic queries by status and name with case-insensitivity
    List<Train> findByStatusIgnoreCase(String status);
    List<Train> findByNameContainingIgnoreCase(String search);
    List<Train> findByNameContainingIgnoreCaseAndStatus(String search, String status);
}
