package com.webtech.rail.rail.service;

import com.webtech.rail.rail.dto.TicketDetailsDTO;
import com.webtech.rail.rail.model.Booking;
import com.webtech.rail.rail.userRepository.BookingRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    // Add getAllBookings for list return
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(Booking booking) {
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingStatus("CONFIRMED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(Long userId, String status) {
        return bookingRepository.findByUser_IdAndBookingStatus(userId, status);
    }

    public List<Booking> getUpcomingBookings(Long userId) {
        return bookingRepository.findByUser_IdAndBookingStatus(userId, "CONFIRMED");
    }

    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setBookingStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    public Page<Booking> findBookings(String search, String status, String s, Pageable pageable) {
        if (search == null) {
            search = "";
        }
        if (status == null || status.isEmpty()) {
            status = "all";
        }

        try {
            if (search.isEmpty() && status.equals("all")) {
                return bookingRepository.findAll(pageable);
            } else if (search.isEmpty()) {
                return bookingRepository.findByBookingStatus(status, pageable);
            } else if (status.equals("all")) {
                return bookingRepository.findByUser_EmailContainingIgnoreCase(search, pageable);
            } else {
                return bookingRepository.findByUser_EmailContainingIgnoreCaseAndBookingStatus(
                        search,
                        status,
                        pageable
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error finding bookings: " + e.getMessage(), e);
        }
    }

    public void exportToExcel(String search, String status, OutputStream outputStream) throws IOException {
        List<Booking> bookings;

        if (search == null) {
            search = "";
        }
        if (status == null || status.isEmpty()) {
            status = "all";
        }

        try {
            if (search.isEmpty() && status.equals("all")) {
                bookings = bookingRepository.findAll();
            } else if (search.isEmpty()) {
                bookings = bookingRepository.findByBookingStatus(status);
            } else if (status.equals("all")) {
                bookings = bookingRepository.findByUser_EmailContainingIgnoreCase(search);
            } else {
                bookings = bookingRepository.findByUser_EmailContainingIgnoreCaseAndBookingStatus(search, status);
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Bookings");

                // Create header row
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("User Email");
                headerRow.createCell(2).setCellValue("From Station");
                headerRow.createCell(3).setCellValue("To Station");
                headerRow.createCell(4).setCellValue("Travel Class");
                headerRow.createCell(5).setCellValue("Departure Date");
                headerRow.createCell(6).setCellValue("Booking Date");
                headerRow.createCell(7).setCellValue("Total Price");
                headerRow.createCell(8).setCellValue("Status");

                // Fill data rows
                int rowNum = 1;
                for (Booking booking : bookings) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(booking.getId());
                    row.createCell(1).setCellValue(booking.getUser().getEmail());
                    row.createCell(2).setCellValue(booking.getFromStation());
                    row.createCell(3).setCellValue(booking.getToStation());
                    row.createCell(4).setCellValue(booking.getTravelClass());
                    row.createCell(5).setCellValue(booking.getDepartureDate().toString());
                    row.createCell(6).setCellValue(booking.getBookingDate().toString());
                    row.createCell(7).setCellValue(booking.getTotalPrice().toString());
                    row.createCell(8).setCellValue(booking.getBookingStatus());
                }

                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new IOException("Error exporting bookings to Excel: " + e.getMessage(), e);
        }
    }

    public TicketDetailsDTO generateTicketDetails(Booking booking) {
        try {
            TicketDetailsDTO ticket = new TicketDetailsDTO();
            ticket.setTicketNumber(generateTicketNumber());
            ticket.setPassengerName(booking.getUser().getFirstname() + " " + booking.getUser().getLastname());
            ticket.setFromStation(booking.getFromStation());
            ticket.setToStation(booking.getToStation());
            ticket.setDepartureTime(booking.getDepartureDate());
            ticket.setArrivalTime(calculateArrivalTime(booking));
            ticket.setTrainNumber(generateTrainNumber());
            ticket.setTrainName("SmartRail Express");
            ticket.setSeatNumber(generateSeatNumber());
            ticket.setTravelClass(booking.getTravelClass());
            ticket.setPrice(booking.getTotalPrice());
            ticket.setStatus(booking.getBookingStatus());
            ticket.setQrCode(generateQRCode(ticket.getTicketNumber()));
            return ticket;
        } catch (Exception e) {
            throw new RuntimeException("Error generating ticket details: " + e.getMessage(), e);
        }
    }

    private String generateTicketNumber() {
        return "SR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTrainNumber() {
        return "TR" + (1000 + new Random().nextInt(9000));
    }

    private String generateSeatNumber() {
        String[] coaches = {"A", "B", "C", "D"};
        int seatNum = 1 + new Random().nextInt(60);
        return coaches[new Random().nextInt(coaches.length)] + seatNum;
    }

    private LocalDateTime calculateArrivalTime(Booking booking) {
        return booking.getDepartureDate().plusHours(3);
    }

    private String generateQRCode(String ticketNumber) {
        return "qr-code-placeholder-" + ticketNumber;
    }
    public List<Booking> searchBookings(String search, String status) {
        return findBookings(search, status, status, Pageable.unpaged()).getContent();
    }

    // Already implemented as exportToExcel
    public void exportBookings(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=bookings.xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            exportToExcel("", "all", outputStream);
        }
    }
}