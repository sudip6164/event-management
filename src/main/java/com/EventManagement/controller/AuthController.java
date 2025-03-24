package com.EventManagement.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.EventManagement.model.User;
import com.EventManagement.repository.UserRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
	@Autowired
    private UserRepository userRepository;
	
    @Autowired
    private HttpSession session;
    
    @Autowired
    private JavaMailSender emailSender;

    private Map<String, String> otpMap = new HashMap<>();
    	
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
            return "index";
        }
        session.setAttribute("loggedIn", false);
        model.addAttribute("error", "Invalid Username or Password!");
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
			return "redirect:/profilePage"; 
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/editProfilePage";
		}
				
	}
	
	@GetMapping("/changePasswordPage")
    public String changePasswordPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/loginPage";
        }
        User user = userRepository.findByUsername(username);
        model.addAttribute("user", user);
        return "user/profile/changePassword";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            Model model,
            HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/loginPage";
        }

        User user = userRepository.findByUsername(username);

        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            model.addAttribute("error", "Current password is incorrect.");
            return "user/profile/changePassword";
        }

        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedNewPassword);
        userRepository.save(user);

        return "redirect:/profilePage";
    }
    
    @GetMapping("/forgotPasswordPage")
    public String forgotPasswordPage() {
        return "user/forgotPassword";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("invalidEmail", true);
            model.addAttribute("emailError", "Invalid Email!");
            return "user/forgotPassword"; // Corrected to go back to forgotPassword page
        }

        String otp = generateOTP();
        System.out.println("Generated OTP for email " + email + ": " + otp);
        otpMap.put(email , otp); // Store the OTP in the map with the email as the key
        try {
            sendOTPEmail(email, otp);
        } catch (RuntimeException e) {
            model.addAttribute("emailError", "Failed to send OTP. Please try again.");
            return "user/forgotPassword";
        }
        
        model.addAttribute("email", email);
        return "user/otp"; // Send the user to OTP verification page
    }

    private String generateOTP() {
        // Generate a 4-digit OTP
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    private void sendOTPEmail(String email, String otp) {
        // Get current timestamp
        long timestamp = System.currentTimeMillis();
        // Combine OTP value and timestamp into a single string separated by ':'
        String otpWithTimestamp = otp + ":" + timestamp;
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        try {
            helper.setTo(email);
            helper.setSubject("Password Reset OTP");
            helper.setText("Your OTP for password reset is: " + otp + "\n\nYour OTP will expire in 5 minutes.");
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email. Please try again.");
        }
        emailSender.send(message);
        // Store OTP with timestamp in the map
        otpMap.put(email, otpWithTimestamp);
    }

    @PostMapping("/verifyOTP")
    public String verifyOTP(@RequestParam("email") String email, @RequestParam("otp") String otp, Model model) {
        String storedOTP = otpMap.get(email); // Retrieve the OTP using the email as the key
        System.out.println("Email received in verifyOTP: " + email);
        System.out.println("OTP received in verifyOTP: " + otp);
        System.out.println("Retrieved OTP for email " + email + ": " + storedOTP);
        
        if (storedOTP != null) {
            // Split the storedOTP to extract OTP value and timestamp
            String[] storedOTPParts = storedOTP.split(":");
            String otpValue = storedOTPParts[0];
            long otpTimestamp = Long.parseLong(storedOTPParts[1]);
            long currentTimestamp = System.currentTimeMillis();

            // Check if OTP is expired (5 minutes)
            if (currentTimestamp - otpTimestamp > 300000) {
                // OTP is expired
                model.addAttribute("expiredOTP", true);
                model.addAttribute("invalidOTPMessage", "Expired OTP code!");
                return "user/otp"; // Redirect back to OTP page
            }

            if (otpValue.equals(otp)) {
                model.addAttribute("email", email);
                // OTP is valid, allow the user to reset password
                return "user/resetPassword"; // Proceed to reset password page
            } else {
                // OTP is invalid
                model.addAttribute("invalidOTP", true);
                model.addAttribute("invalidOTPMessage", "Invalid OTP code!");
                model.addAttribute("otp", otp);
                return "user/otp"; // Return to OTP page with error
            }
        } else {
            // No OTP found for the email
            model.addAttribute("invalidOTP", true);
            model.addAttribute("invalidOTPMessage", "Invalid OTP code!");
            return "user/otp"; // Return to OTP page with error
        }
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        User user = userRepository.findByEmail(email);
        System.out.println(email);
        System.out.println(user);
        if (user == null) {
            model.addAttribute("error", "User not found.");
            return "user/forgotPassword"; // Go back to forgotPassword page if user not found
        }
        // Hash the new password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        // Clear OTP from map after password reset
        otpMap.remove(email);

        model.addAttribute("passwordResetSuccess", true);
        model.addAttribute("resetSuccess", "Password reset successfully.");
        return "redirect:/loginPage"; // Redirect to login page after successful password reset
    }

}
