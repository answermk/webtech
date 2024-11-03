package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.model.DateRange;
import com.webtech.rail.rail.model.Report;
import com.webtech.rail.rail.model.ReportType;
import com.webtech.rail.rail.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

// ReportsController.java (Controller)
@Controller
@RequestMapping("/admin/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String showReportsPage(Model model) {
        model.addAttribute("reportTitle", "Operational Performance Report");
        model.addAttribute("reportDateRange", "Oct 1, 2024 - Oct 16, 2024");
        return "reports";
    }

    @PostMapping
    public String generateReport(@RequestParam ReportType reportType,
                                 @RequestParam DateRange dateRange,
                                 @RequestParam(required = false) LocalDate startDate,
                                 @RequestParam(required = false) LocalDate endDate,
                                 Model model) {
        Report report = reportService.generateReport(reportType, dateRange, startDate, endDate);
        model.addAttribute("reportTitle", report.getTitle());
        model.addAttribute("reportDateRange", report.getDateRangeText());
        model.addAttribute("metrics", report.getMetrics());
        return "reports";
    }
}
