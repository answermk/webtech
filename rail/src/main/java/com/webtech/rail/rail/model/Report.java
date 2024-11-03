package com.webtech.rail.rail.model;

import java.time.LocalDate;
import java.util.Map;

// Report.java (Model)
public class Report {
    private ReportType reportType;
    private DateRange dateRange;
    private LocalDate startDate;
    private LocalDate endDate;
    private String title;
    private String dateRangeText;
    private Map<String, Object> metrics;

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateRangeText() {
        return dateRangeText;
    }

    public void setDateRangeText(String dateRangeText) {
        this.dateRangeText = dateRangeText;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
// Getters, setters, and constructors
}
