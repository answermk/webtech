package com.webtech.rail.rail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.webtech.rail.rail.model.Contact;
import com.webtech.rail.rail.userRepository.ContactRepository;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public boolean saveContact(Contact contact) {
        try {
            contactRepository.save(contact);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}