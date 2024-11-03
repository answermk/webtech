package com.webtech.rail.rail.service;

import com.webtech.rail.rail.model.Booking;
import com.webtech.rail.rail.model.Train;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;

@Service
public class ExcelExportService {

    private <T> void exportToExcel(List<T> data, String sheetName, OutputStream outputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        if (!data.isEmpty()) {
            T firstItem = data.get(0);
            Field[] fields = firstItem.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields[i].getName());
            }
        }

        // Create data rows
        int rowNum = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowNum++);
            Field[] fields = item.getClass().getDeclaredFields();
            int cellNum = 0;
            for (Field field : fields) {
                field.setAccessible(true);
                Cell cell = row.createCell(cellNum++);
                try {
                    Object value = field.get(item);
                    cell.setCellValue(value != null ? value.toString() : "");
                } catch (IllegalAccessException e) {
                    cell.setCellValue("");
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output stream
        workbook.write(outputStream);
        workbook.close();
    }

    public void exportTrains(List<Train> trains, OutputStream outputStream) throws IOException {
        exportToExcel(trains, "Trains", outputStream);
    }

    public void exportBookings(List<Booking> bookings, OutputStream outputStream) throws IOException {
        exportToExcel(bookings, "Bookings", outputStream);
    }

    public void exportSchedules(List<?> schedules, OutputStream outputStream) throws IOException {
        exportToExcel(schedules, "Schedules", outputStream);
    }
}