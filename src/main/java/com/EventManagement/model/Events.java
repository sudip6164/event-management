package com.EventManagement.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Events {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String eventName;
    private String venue;
    private LocalDate eventDate;
    private LocalTime eventStartTime;
    private LocalTime eventEndTime;
    private String description;
    
    private String eventsPictureName;
	@Transient
	private MultipartFile eventsPicture;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getVenue() {
		return venue;
	}
	public void setVenue(String venue) {
		this.venue = venue;
	}
	public LocalDate getEventDate() {
		return eventDate;
	}
	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}
	public LocalTime getEventStartTime() {
		return eventStartTime;
	}
	public void setEventStartTime(LocalTime eventStartTime) {
		this.eventStartTime = eventStartTime;
	}
	public LocalTime getEventEndTime() {
		return eventEndTime;
	}
	public void setEventEndTime(LocalTime eventEndTime) {
		this.eventEndTime = eventEndTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getEventsPictureName() {
		return eventsPictureName;
	}
	public void setEventsPictureName(String eventsPictureName) {
		this.eventsPictureName = eventsPictureName;
	}
	public MultipartFile getEventsPicture() {
		return eventsPicture;
	}
	public void setEventsPicture(MultipartFile eventsPicture) {
		this.eventsPicture = eventsPicture;
	}
	
}
