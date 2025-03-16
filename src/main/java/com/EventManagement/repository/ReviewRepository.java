package com.EventManagement.repository;

import org.springframework.stereotype.Repository;

import com.EventManagement.model.Review;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer>{

}
