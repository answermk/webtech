package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.DateRange;
import com.webtech.rail.rail.model.Report;
import com.webtech.rail.rail.model.ReportType;
import com.webtech.rail.rail.userRepository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report generateReport(ReportType reportType, DateRange dateRange, LocalDate startDate, LocalDate endDate) {
        return reportRepository.generateReport(reportType, dateRange, startDate, endDate);
    }
}