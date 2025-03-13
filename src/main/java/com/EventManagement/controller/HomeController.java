package com.EventManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.EventManagement.model.User;
import com.EventManagement.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
    private UserRepository userRepository;
	
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
    public String aboutPage() {
        return "about";
    }
	
	@GetMapping("/eventsPage")
    public String eventsPage() {
        return "events";
    }
	
	@GetMapping("/testimonialsPage")
    public String testimonialsPage() {
        return "testimonials";
    }
	
	@GetMapping("/contactPage")
    public String contactPage() {
        return "contact";
    }
}
