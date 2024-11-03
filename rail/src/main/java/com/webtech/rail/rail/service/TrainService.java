package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.Train;
import com.webtech.rail.rail.userRepository.TrainRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TrainService {
    @Autowired
    private TrainRepository trainRepository;

    public List<Train> getAllTrains() {
        List<Train> trains = trainRepository.findAll();
        if (trains.isEmpty()) {
            System.out.println("No trains found in the database");
        }
        return trains;
    }
    public List<Train> searchTrainsByStatus(String status) {
        return getTrainsByStatus(status);
    }
    private static final Logger logger = LoggerFactory.getLogger(TrainService.class);

    public Page<Train> findTrains(String search, String status, Pageable pageable) {
        logger.info("Searching trains with status: {}, search: {}, page: {}",
                status, search, pageable.getPageNumber());

        Page<Train> results;
        if (status != null && !status.equalsIgnoreCase("all")) {
            results = new PageImpl<>(trainRepository.findByStatusIgnoreCase(status));
            logger.info("Found {} trains with status {}", results.getTotalElements(), status);
        } else if (!StringUtils.isEmpty(search)) {
            results = new PageImpl<>(trainRepository.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(
                    search, search));
            logger.info("Found {} trains matching search term {}", results.getTotalElements(), search);
        } else {
            results = trainRepository.findAll(pageable);
            logger.info("Found {} total trains", results.getTotalElements());
        }

        return results;
    }

    // Already implemented as exportToExcel
    public void exportTrains(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=trains.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            exportToExcel("", "all", outputStream);
        }
    }

    public Train getTrainById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
    }

    public void addTrain(Train train) {
        trainRepository.save(train);
        train.setStatus(train.getStatus()); // This might be redundant if already set
    }

    public void updateTrain(Train train) {
        if (!trainRepository.existsById(train.getTrainId())) {
            throw new RuntimeException("Train not found with id: " + train.getTrainId());
        }
        trainRepository.save(train);
    }

    public void deleteTrain(Long id) {
        if (!trainRepository.existsById(id)) {
            throw new RuntimeException("Train not found with id: " + id);
        }
        trainRepository.deleteById(id);
    }

    public List<Train> getTrainsByStatus(String status) {
        return trainRepository.findByStatusIgnoreCase(status); // uses the clarified method name
    }

    public Page<Train> getTrainsByStatus(String status, Pageable pageable) {
        return trainRepository.findByStatus(status, pageable);
    }


    // New method to count trains by status
    public long countTrainsByStatus(String status) {
        return trainRepository.findByStatusIgnoreCase(status).size();
    }


    // New method to calculate active trains growth
    public double calculateActiveTrainsGrowth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minus(1, ChronoUnit.MONTHS);
        long previousActiveTrains = trainRepository.countByStatusAndCreatedDateBefore("ACTIVE", oneMonthAgo);

        // Get current count of active trains
        long currentActiveTrains = countTrainsByStatus("ACTIVE");

        // Get count of active trains from one month ago
        // Note: You'll need to modify this based on your actual data model
        // This is just an example implementation
      //  long previousActiveTrains = trainRepository.findByStatusAndCreatedDateBefore("ACTIVE", oneMonthAgo).size();


        // Calculate growth rate
        if (previousActiveTrains == 0) {
            return currentActiveTrains > 0 ? 100.0 : 0.0; // 100% growth if previous was 0 and current > 0
        }

        return ((double) (currentActiveTrains - previousActiveTrains) / previousActiveTrains) * 100.0;
    }
    public Page<Train> findTrainsByStatus(String status, Pageable pageable) {
        if (status == null || status.equalsIgnoreCase("all")) {
            return trainRepository.findAll(pageable);
        }
        return trainRepository.findByStatus(status, pageable);
    }

    public List<Train> getActiveTrainsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return trainRepository.findActiveTrainsInPeriod("ACTIVE", startDate, endDate);
    }

 //   public Page<Train> findTrains(String search, String status, Pageable pageable) {
   //     if (search.isEmpty() && status.equals("all")) {
     //       return trainRepository.findAll(pageable);
       // } else if (search.isEmpty()) {
         //   return trainRepository.findByStatus(status, pageable);
    //    } else if (status.equals("all")) {
      //      return trainRepository.findByNameContainingIgnoreCase(search, pageable);
        //} else {
          //  return trainRepository.findByNameContainingIgnoreCaseAndStatus(search, status, pageable);
        //}
    //}

    public void exportToExcel(String search, String status, OutputStream outputStream) throws IOException {
        List<Train> trains;
        try {
            // Get trains based on search criteria
            if (search.isEmpty() && status.equals("all")) {
                trains = trainRepository.findAll();
            } else if (search.isEmpty()) {
                trains = trainRepository.findByStatusIgnoreCase(status);
            } else if (status.equals("all")) {
                trains = trainRepository.findByNameContainingIgnoreCase(search);
            } else {
                trains = trainRepository.findByNameContainingIgnoreCaseAndStatus(search, status);
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Trains Report");

                // Create cell style for headers
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);

                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // Create cell style for data
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                // Create headers
                Row headerRow = sheet.createRow(0);
                String[] columns = {"ID", "Name", "Type", "Capacity", "Status", "Created Date"};

                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Create data rows
                int rowNum = 1;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                for (Train train : trains) {
                    Row row = sheet.createRow(rowNum++);

                    Cell idCell = row.createCell(0);
                    idCell.setCellValue(train.getTrainId());
                    idCell.setCellStyle(dataStyle);

                    Cell nameCell = row.createCell(1);
                    nameCell.setCellValue(train.getName());
                    nameCell.setCellStyle(dataStyle);

                    Cell typeCell = row.createCell(2);
                    typeCell.setCellValue(train.getType());
                    typeCell.setCellStyle(dataStyle);

                    Cell capacityCell = row.createCell(3);
                    capacityCell.setCellValue(train.getCapacity());
                    capacityCell.setCellStyle(dataStyle);

                    Cell statusCell = row.createCell(4);
                    statusCell.setCellValue(train.getStatus());
                    statusCell.setCellStyle(dataStyle);

                    Cell dateCell = row.createCell(5);
                    if (train.getCreatedDate() != null) {
                        dateCell.setCellValue(train.getCreatedDate().format(formatter));
                    }
                    dateCell.setCellStyle(dataStyle);
                }

                // Autosize columns
                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Add title and export info
                sheet.createRow(sheet.getLastRowNum() + 2)
                        .createCell(0)
                        .setCellValue("Generated on: " + LocalDateTime.now().format(formatter));

                if (!search.isEmpty() || !status.equals("all")) {
                    Row filterRow = sheet.createRow(sheet.getLastRowNum() + 1);
                    filterRow.createCell(0).setCellValue("Filters applied - Search: " +
                            (search.isEmpty() ? "None" : search) + ", Status: " + status);
                }

                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new IOException("Failed to export train data: " + e.getMessage(), e);
        }
    }

}
