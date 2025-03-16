package com.EventManagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Contact {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String contactName;
    private String contactEmail;
    private String contactSubject;
    private String contactMessage;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactEmail() {
		return contactEmail;
	}
	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
	public String getContactSubject() {
		return contactSubject;
	}
	public void setContactSubject(String contactSubject) {
		this.contactSubject = contactSubject;
	}
	public String getContactMessage() {
		return contactMessage;
	}
	public void setContactMessage(String contactMessage) {
		this.contactMessage = contactMessage;
	}
    
}
