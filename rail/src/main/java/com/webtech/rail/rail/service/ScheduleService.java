package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.Schedule;
import com.webtech.rail.rail.model.ScheduleStatus;
import com.webtech.rail.rail.userRepository.RouteRepository;
import com.webtech.rail.rail.userRepository.ScheduleRepository;
import com.webtech.rail.rail.userRepository.TrainRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private RouteRepository routeRepository;

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        validateSchedule(schedule);
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        validateSchedule(schedule);
        if (!scheduleRepository.existsById(schedule.getId())) {
            throw new IllegalArgumentException("Schedule not found with id: " + schedule.getId());
        }
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void cancelSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));
        schedule.setStatus(ScheduleStatus.CANCELLED);
        scheduleRepository.save(schedule);
    }

    private void validateSchedule(Schedule schedule) {
        if (schedule.getDeparture().isAfter(schedule.getArrival())) {
            throw new IllegalArgumentException("Departure time cannot be after arrival time");
        }
        if (schedule.getTrain() == null) {
            throw new IllegalArgumentException("Train must be specified");
        }
        if (schedule.getRoute() == null) {
            throw new IllegalArgumentException("Route must be specified");
        }
    }

    public void exportSchedules(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=schedules.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            exportToExcel("", "all", outputStream);
        }
    }

    public Page<Schedule> findSchedules(String search, String status, String s, Pageable pageable) {
        if (search == null) {
            search = "";
        }
        if (status == null || status.isEmpty()) {
            status = "all";
        }

        try {
            if (search.isEmpty() && status.equals("all")) {
                return scheduleRepository.findAll(pageable);
            } else if (search.isEmpty()) {
                return scheduleRepository.findByStatus(ScheduleStatus.valueOf(status.toUpperCase()), pageable);
            } else {
                // Convert search string to Long for train_TrainId
                try {
                    Long trainId = Long.parseLong(search);
                    if (status.equals("all")) {
                        return new PageImpl<>(scheduleRepository.findByTrain_TrainId(trainId));
                    } else {
                        return new PageImpl<>(scheduleRepository.findByTrain_TrainIdAndStatus(
                                trainId,
                                ScheduleStatus.valueOf(status.toUpperCase())
                        ));
                    }
                } catch (NumberFormatException e) {
                    return Page.empty();
                }
            }
        } catch (IllegalArgumentException e) {
            return Page.empty();
        }
    }

    public void exportToExcel(String search, String status, OutputStream outputStream) throws IOException {
        List<Schedule> schedules;
        Pageable unpaged = Pageable.unpaged();

        if (search == null) {
            search = "";
        }
        if (status == null || status.isEmpty()) {
            status = "all";
        }

        try {
            if (search.isEmpty() && status.equals("all")) {
                schedules = scheduleRepository.findAll();
            } else if (search.isEmpty()) {
                schedules = scheduleRepository.findByStatus(
                        ScheduleStatus.valueOf(status.toUpperCase()),
                        unpaged
                ).getContent();
            } else {
                // Convert search string to Long for train_TrainId
                try {
                    Long trainId = Long.parseLong(search);
                    if (status.equals("all")) {
                        schedules = scheduleRepository.findByTrain_TrainId(trainId);
                    } else {
                        schedules = scheduleRepository.findByTrain_TrainIdAndStatus(
                                trainId,
                                ScheduleStatus.valueOf(status.toUpperCase())
                        );
                    }
                } catch (NumberFormatException e) {
                    schedules = Collections.emptyList();
                }
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Schedules");

                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Train ID");
                headerRow.createCell(2).setCellValue("Departure");
                headerRow.createCell(3).setCellValue("Arrival");
                headerRow.createCell(4).setCellValue("Status");

                int rowNum = 1;
                for (Schedule schedule : schedules) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(schedule.getId());
                    row.createCell(1).setCellValue(schedule.getTrain().getTrainId());
                    row.createCell(2).setCellValue(schedule.getDeparture().toString());
                    row.createCell(3).setCellValue(schedule.getArrival().toString());
                    row.createCell(4).setCellValue(schedule.getStatus().toString());
                }

                workbook.write(outputStream);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid status value: " + status, e);
        }
    }

    public List<Schedule> findByTrain_TrainId(Long trainId) {
        return scheduleRepository.findByTrain_TrainId(trainId);
    }
}