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
public class AdminController {
	@Autowired
    private UserRepository userRepository;
	
    @Autowired
    private HttpSession session;
    
	@GetMapping("/admin")
    public String adminDashboard() {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    return "admin/dashboard";
    }
	
	@GetMapping("/admin/loginPage")
    public String adminLoginPage() {
        return "admin/adminLogin";
    }
	
	@PostMapping("/admin/login")
    public String adminLogin(@ModelAttribute User u, Model model) {
        User user = userRepository.findByUsername(u.getUsername());
        if (user != null && BCrypt.checkpw(u.getPassword(), user.getPassword())) {
            if (u.getUsername().equals("admin") && u.getPassword().equals("Admin@123")) {
                session.setMaxInactiveInterval(3600);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("adminUser", true);
                return "redirect:/admin";
            } else {
                session.setAttribute("adminUser", false);
                return "redirect:/admin/loginPage";
            }
        }
        session.setAttribute("message", "Invalid email or password");
        return "redirect:/admin/loginPage";
    }
	
	@GetMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/loginPage";
    }
	
	@GetMapping("/admin/userList")
	public String UserTable(@ModelAttribute User u, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		model.addAttribute("userList", userRepository.findAll());
		return "admin/manageUser";
	}
}
