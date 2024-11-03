package com.webtech.rail.rail.userRepository;


import com.webtech.rail.rail.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContactRepository extends JpaRepository<Contact, Long> {
}
