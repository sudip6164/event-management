package com.EventManagement.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.EventManagement.model.User;
import com.EventManagement.repository.UserRepository;

@Controller
public class AuthController {
	@Autowired
    private UserRepository userRepository;
	
	@GetMapping("/registerPage")
    public String registerPage() {
        return "user/register.html";
    }
	
	@PostMapping("/register")
	public String register(@ModelAttribute("user") User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("errorUserExists", "User already exists!");
            return "register.html";
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        

        userRepository.save(user);

        return "index.html";
    }
}
