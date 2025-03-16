package com.EventManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.EventManagement.model.Booking;
import com.EventManagement.model.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer>{
	List<Booking> findByUser(User user);
}
