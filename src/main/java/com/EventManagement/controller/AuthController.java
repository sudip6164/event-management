package com.EventManagement.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    
    @GetMapping("/profilePage")
    public String showProfile(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "user/profile/profile"; 
        }
        return "redirect:/loginPage"; 
    }
    
    @GetMapping("/editProfilePage")
    public String editProfilePage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "user/profile/editProfile"; 
        }
        return "redirect:/loginPage";
    }

	@PostMapping("/editProfile")
	public String updateProfile(@ModelAttribute("user") User user,
			@RequestParam("profilePicture") MultipartFile profilePicture, HttpSession session) {
		String username = (String) session.getAttribute("username");
		if (username == null) {
			return "redirect:/loginPage";
		}

		try {
			User existingUser = userRepository.findByUsername(username);
			if (existingUser == null) {
				return "redirect:/loginPage";
			}

			existingUser.setEmail(user.getEmail());
			existingUser.setPhone(user.getPhone());
			existingUser.setLocation(user.getLocation());
			existingUser.setBio(user.getBio());

			if (!profilePicture.isEmpty()) {

				String imagePath = "src/main/resources/static/images/profile/" + profilePicture.getOriginalFilename();
				File saveFile = new File(imagePath);
				Path savePath = saveFile.toPath();

				saveFile.getParentFile().mkdirs();

				Files.copy(profilePicture.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
				existingUser.setProfilePictureName(profilePicture.getOriginalFilename());

				System.out.println("Profile picture saved at: " + savePath);
			}

			userRepository.save(existingUser);
			return "redirect:/"; 
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/editProfilePage";
		}
	}
}
