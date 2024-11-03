package com.webtech.rail.rail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainStatsDto{
    private long activeTrainsCount;
    private double activeTrainsGrowth;
}
