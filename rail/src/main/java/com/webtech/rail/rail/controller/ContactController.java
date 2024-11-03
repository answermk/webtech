package com.webtech.rail.rail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.webtech.rail.rail.model.Contact;
import com.webtech.rail.rail.service.ContactService;

@Controller
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping("/contact")
    public String submitContact(Contact contact, RedirectAttributes redirectAttributes) {
        boolean isSaved = contactService.saveContact(contact);

        if (isSaved) {
            redirectAttributes.addFlashAttribute("successMessage", "Message sent successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send message. Please try again.");
        }

        return "redirect:/home";
    }
}