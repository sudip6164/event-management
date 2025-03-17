package com.EventManagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.EventManagement.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	User findByUsername(String username);
	User findByEmail(String email);
	@Query("SELECT u FROM User u ORDER BY u.id DESC LIMIT 2")
	List<User> findLatestUsers();
}
