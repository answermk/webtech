package com.webtech.rail.rail.controller;

import com.webtech.rail.rail.dto.TicketDetailsDTO;
import com.webtech.rail.rail.model.Booking;
import com.webtech.rail.rail.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/book/new")
    public String showBookingForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "book";
    }

    @PostMapping("/book")
    public String createBooking(@ModelAttribute Booking booking, RedirectAttributes redirectAttributes) {
        try {
            bookingService.createBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create booking.");
        }
        return "redirect:/dashboardU";
    }

    @GetMapping("/bookings")
    public String showMyBookings(Model model, @RequestParam(required = false) String bookingStatus) {
        Long userId = getCurrentUserId(); // Obtain the current user ID (implement this method)
        List<Booking> bookings = bookingService.getUserBookings(userId, bookingStatus);
        model.addAttribute("bookings", bookings);
        return "book";
    }

    @PostMapping("/book/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel booking.");
        }
        return "redirect:/bookings";
    }

    @PostMapping("/book/confirm")
    public String confirmBooking(@ModelAttribute Booking booking, Model model) {
        booking = bookingService.createBooking(booking);
        TicketDetailsDTO ticketDetails = bookingService.generateTicketDetails(booking);
        model.addAttribute("ticket", ticketDetails);
        return "booking-confirmation";
    }

    private Long getCurrentUserId() {
        // Implement user session handling to retrieve the current user ID
        throw new RuntimeException("User details not found");
    }




    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<List<Booking>> getUpcomingBookings(@PathVariable Long userId) {
        List<Booking> upcomingBookings = bookingService.getUpcomingBookings(userId);
        return ResponseEntity.ok(upcomingBookings);
    }




}
