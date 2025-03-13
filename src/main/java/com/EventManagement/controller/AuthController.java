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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
	@Autowired
    private UserRepository userRepository;
	
    @Autowired
    private HttpSession session;
    	
	@GetMapping("/registerPage")
    public String registerPage() {
        return "user/register";
    }
	
	@PostMapping("/register")
	public String register(@ModelAttribute("user") User user, Model model) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("errorUserExists", "User already exists!");
            return "user/register";
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        

        userRepository.save(user);

        return "redirect:/loginPage";
    }
	
	@GetMapping("/loginPage")
    public String loginPage() {
        return "user/login";
    }
    
    @PostMapping("/login")
    public String Login(@ModelAttribute User u, Model model) {
        User user = userRepository.findByUsername(u.getUsername());
        if (user != null && BCrypt.checkpw(u.getPassword(), user.getPassword())) {

            session.setMaxInactiveInterval(3600);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("loggedIn", true);
//            if (u.getUsername().equals("admin") && u.getPassword().equals("admin")) {
//                return "redirect:/admin";
//            } else {
//                return "index.html";
//            }
            return "redirect:/";
        }
        session.setAttribute("loggedIn", false);
        return "user/login";
    }
    
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
