package com.EventManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.EventManagement.model.Events;
import com.EventManagement.model.User;
import com.EventManagement.repository.EventsRepository;
import com.EventManagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private EventsRepository eventsRepository;
	
    @Autowired
    private HttpSession session;
    
	@GetMapping("/")
    public String indexPage(Model model) {
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "index"; 
        }
        return "index";
    }
	
	@GetMapping("/aboutPage")
    public String aboutPage(Model model) {
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "about"; 
        }
        return "about";
    }
	
	@GetMapping("/eventsPage")
    public String eventsPage(Model model) {
		model.addAttribute("eventsList", eventsRepository.findAll());
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "events"; 
        }
        return "events";
    }
	
	@GetMapping("/eventsDetails/{id}")
	public String ProductDetailsPage(@PathVariable int id, Model model) {
		Events events = eventsRepository.getById(id);
		model.addAttribute("events", events);
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "eventDetails"; 
        }
		return "eventDetails";
	}
	
	@GetMapping("/testimonialsPage")
    public String testimonialsPage(Model model) {
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "testimonials"; 
        }
        return "testimonials";
    }
	
	@GetMapping("/contactPage")
    public String contactPage(Model model) {
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "contact"; 
        }
        return "contact";
    }
}
