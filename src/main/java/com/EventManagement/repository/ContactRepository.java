package com.EventManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.EventManagement.model.Contact;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer>{

}
