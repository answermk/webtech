package com.webtech.rail.rail.dto;

public class DashboardStats {
    private long activeTrainsCount;
    private long todayBookingsCount;
    private long delayedTrainsCount;
    private long activeUsersCount;

    public long getActiveTrainsCount() {
        return activeTrainsCount;
    }

    public void setActiveTrainsCount(long activeTrainsCount) {
        this.activeTrainsCount = activeTrainsCount;
    }

    public long getTodayBookingsCount() {
        return todayBookingsCount;
    }

    public void setTodayBookingsCount(long todayBookingsCount) {
        this.todayBookingsCount = todayBookingsCount;
    }

    public long getDelayedTrainsCount() {
        return delayedTrainsCount;
    }

    public void setDelayedTrainsCount(long delayedTrainsCount) {
        this.delayedTrainsCount = delayedTrainsCount;
    }

    public long getActiveUsersCount() {
        return activeUsersCount;
    }

    public void setActiveUsersCount(long activeUsersCount) {
        this.activeUsersCount = activeUsersCount;
    }
// Getters and setters
}