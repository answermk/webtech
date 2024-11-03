package com.webtech.rail.rail.service;

import com.webtech.rail.rail.dto.TrainStatsDto;
import com.webtech.rail.rail.model.Train;
import com.webtech.rail.rail.userRepository.TrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainStatsService {
    private final TrainRepository trainRepository;

    public TrainStatsDto getActiveTrainsStats() {
        long currentActiveTrains = trainRepository.countByStatusIgnoreCase("active");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        LocalDateTime twoMonthsAgo = now.minusMonths(2);

        long previousPeriodActive = trainRepository.countActiveTrainsInPeriod(twoMonthsAgo, oneMonthAgo);

        double growthPercentage = 0.0;
        if (previousPeriodActive > 0) {
            growthPercentage = ((double)(currentActiveTrains - previousPeriodActive) / previousPeriodActive) * 100;
        }

        return new TrainStatsDto(
                currentActiveTrains,
                Math.round(growthPercentage * 10.0) / 10.0  // Round to 1 decimal place
        );
    }
}
