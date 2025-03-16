package com.EventManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.EventManagement.model.Events;
@Repository
public interface EventsRepository  extends JpaRepository<Events, Integer>{

}
