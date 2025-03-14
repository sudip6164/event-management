package com.EventManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.EventManagement.model.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

	User findByUsername(String username);
	User findByEmail(String email);

}
