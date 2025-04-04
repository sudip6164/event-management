package com.EventManagement.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.EventManagement.model.User;
import com.EventManagement.model.Booking;
import com.EventManagement.model.Events;
import com.EventManagement.repository.UserRepository;
import com.EventManagement.repository.BookingRepository;
import com.EventManagement.repository.EventsRepository;
import com.EventManagement.repository.ReviewRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private EventsRepository eventsRepository;
	
	@Autowired
    private BookingRepository bookingRepository;
	
	@Autowired
    private ReviewRepository reviewRepository;
	
    @Autowired
    private HttpSession session;
    
    @Autowired
    private JavaMailSender emailSender;
    
	@GetMapping("/admin")
    public String adminDashboard(Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    
	    long totalEvents = eventsRepository.count();
	    long totalUsers = userRepository.count();
	    long totalBookings = bookingRepository.count();
	    
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBookings", totalBookings);
        
		List<User> latestUsers = userRepository.findLatestUsers();
	    model.addAttribute("latestUsers", latestUsers);
	    
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
	
	@GetMapping("/admin/userEditPage/{id}")
	public String userEditPage(@PathVariable int id, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    User user = userRepository.getById(id);
	    model.addAttribute("user", user);
	    return "admin/editUser";
	}
	
	@PostMapping("/admin/userEdit")
	public String userEdit(@ModelAttribute User user, @RequestParam("profilePicture") MultipartFile profilePicture, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    
	    User existingUser = userRepository.findById(user.getId()).orElse(null);
	    
	    existingUser.setUsername(user.getUsername());
	    existingUser.setEmail(user.getEmail());
	    existingUser.setPhone(user.getPhone());
	    existingUser.setLocation(user.getLocation());
	    existingUser.setBio(user.getBio());
	    
	    if (!profilePicture.isEmpty()) {

			String imagePath = "src/main/resources/static/images/profile/" + profilePicture.getOriginalFilename();
			File saveFile = new File(imagePath);
			Path savePath = saveFile.toPath();

			saveFile.getParentFile().mkdirs();

			try {
				Files.copy(profilePicture.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			existingUser.setProfilePictureName(profilePicture.getOriginalFilename());

			System.out.println("Profile picture saved at: " + savePath);
		}

	    userRepository.save(existingUser);
	    return "redirect:/admin/userList";
	}
	
	@GetMapping("admin/userDelete/{id}")
	public String userdelete(@PathVariable int id,	Model model)
	{
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		userRepository.deleteById(id);
		return "redirect:/admin/userList";
	}
	
	@GetMapping("/admin/eventsList")
	public String EventsTable(@ModelAttribute User u, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		model.addAttribute("eventsList", eventsRepository.findAll());
		return "admin/manageEvents";
	}
	
	@GetMapping("/admin/eventsAddPage")
    public String eventsAddPage() {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
        return "admin/addEvents";
    }
	
	@PostMapping("/admin/eventsAdd")
	public String eventsAdd(@ModelAttribute("events") Events events,  @RequestParam("eventsPicture") MultipartFile eventsPicture, Model model) {
		
		if (!eventsPicture.isEmpty()) {

			String imagePath = "src/main/resources/static/images/events/" + eventsPicture.getOriginalFilename();
			File saveFile = new File(imagePath);
			Path savePath = saveFile.toPath();

			saveFile.getParentFile().mkdirs();

			try {
				Files.copy(eventsPicture.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			events.setEventsPictureName(eventsPicture.getOriginalFilename());

			System.out.println("Events picture saved at: " + savePath);
		}

		eventsRepository.save(events);

        return "redirect:/admin/eventsList";
    }
	
	@GetMapping("/admin/eventsEditPage/{id}")
	public String eventsEditPage(@PathVariable int id, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    Events events = eventsRepository.getById(id);
	    model.addAttribute("events", events);
	    return "admin/editEvents";
	}
	
	@PostMapping("/admin/eventsEdit")
	public String eventsEdit(@ModelAttribute Events events, @RequestParam("eventsPicture") MultipartFile eventsPicture,@RequestParam("standardPrice") double standardPrice,
            @RequestParam("vipPrice") double vipPrice,
            @RequestParam("premiumPrice") double premiumPrice, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    
	    Events existingEvents = eventsRepository.findById(events.getId()).orElse(null);
	    
	    existingEvents.setEventName(events.getEventName());
	    existingEvents.setVenue(events.getVenue());
	    if (events.getEventDate() != null) {
	        existingEvents.setEventDate(events.getEventDate());
	    }
	    if (events.getEventStartTime() != null) {
	        existingEvents.setEventStartTime(events.getEventStartTime());
	    }
	    if (events.getEventEndTime() != null) {
	        existingEvents.setEventEndTime(events.getEventEndTime());
	    }
	    
	    existingEvents.setStandardPrice(standardPrice);
	    existingEvents.setVipPrice(vipPrice);
	    existingEvents.setPremiumPrice(premiumPrice);
	    
	    if (!eventsPicture.isEmpty()) {

			String imagePath = "src/main/resources/static/images/events/" + eventsPicture.getOriginalFilename();
			File saveFile = new File(imagePath);
			Path savePath = saveFile.toPath();

			saveFile.getParentFile().mkdirs();

			try {
				Files.copy(eventsPicture.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			existingEvents.setEventsPictureName(eventsPicture.getOriginalFilename());

			System.out.println("Profile picture saved at: " + savePath);
		}

	    eventsRepository.save(existingEvents);
	    return "redirect:/admin/eventsList";
	}
	
	@GetMapping("admin/eventsDelete/{id}")
	public String eventsdelete(@PathVariable int id, Model model)
	{
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		eventsRepository.deleteById(id);
		return "redirect:/admin/eventsList";
	}
	
	@GetMapping("/admin/bookingList")
	public String BookingTable(@ModelAttribute User u, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		model.addAttribute("bookingList", bookingRepository.findAll());
		return "admin/manageBooking";
	}
	
	@GetMapping("admin/bookingDelete/{id}")
	public String bookingdelete(@PathVariable int id, Model model)
	{
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		bookingRepository.deleteById(id);
		return "redirect:/admin/bookingList";
	}
	
	@GetMapping("/admin/bookingEditPage/{id}")
	public String bookingEditPage(@PathVariable int id, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    Booking booking = bookingRepository.getById(id);
	    model.addAttribute("booking", booking);
	    return "admin/editBooking";
	}
	
	@PostMapping("/admin/bookingEdit")
	public String userEdit(@ModelAttribute Booking booking, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
	    
	    Booking existingBooking= bookingRepository.findById(booking.getId()).orElse(null);
	    
	    existingBooking.setPaymentStatus(booking.getPaymentStatus());
	    
	    bookingRepository.save(existingBooking);
	    return "redirect:/admin/bookingList";
	}
	
	@GetMapping("/admin/reviewList")
	public String reviewTable(@ModelAttribute User u, Model model) {
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		model.addAttribute("reviewList", reviewRepository.findAll());
		return "admin/manageReview";
	}
	
	@GetMapping("admin/reviewDelete/{id}")
	public String reviewdelete(@PathVariable int id, Model model)
	{
		Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
	    if (isAdmin == null || !isAdmin) {
	        return "redirect:/admin/loginPage";
	    }
		reviewRepository.deleteById(id);
		return "redirect:/admin/reviewList";
	}
	
	@GetMapping("/admin/bookingApprove/{id}")
    public String approveBooking(@PathVariable int id, Model model) {
        Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
        if (isAdmin == null || !isAdmin) {
            return "redirect:/admin/loginPage";
        }
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setBookingStatus(Booking.BookingStatus.APPROVED);
            bookingRepository.save(booking);
            sendBookingStatusEmail(booking, "APPROVED"); // Send email
        } else {
            System.out.println("Booking not found for ID: " + id);
        }
        return "redirect:/admin/bookingList";
    }

    @GetMapping("/admin/bookingDeny/{id}")
    public String denyBooking(@PathVariable int id, Model model) {
        Boolean isAdmin = (Boolean) session.getAttribute("adminUser");
        if (isAdmin == null || !isAdmin) {
            return "redirect:/admin/loginPage";
        }
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            booking.setBookingStatus(Booking.BookingStatus.DENIED);
            bookingRepository.save(booking);
            sendBookingStatusEmail(booking, "DENIED"); // Send email
        } else {
            System.out.println("Booking not found for ID: " + id);
        }
        return "redirect:/admin/bookingList";
    }

    // Helper method to send email
    private void sendBookingStatusEmail(Booking booking, String status) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("infoyarsatoursandtravels@gmail.com"); // Sender email
            helper.setTo(booking.getUser().getEmail()); // User's email
            helper.setSubject("Booking Status Update - EventTalk");

            String emailContent = String.format(
                "Dear %s,\n\n" +
                "We are writing to inform you about the status of your booking with EventTalk.\n\n" +
                "Booking Details:\n" +
                "-------------------\n" +
                "Booking ID: %d\n" +
                "Event Name: %s\n" +
                "Ticket Type: %s\n" +
                "Price: %.2f\n" +
                "Payment Status: %s\n" +
                "Booking Status: %s\n" +
                "-------------------\n\n" +
                "If you have any questions, feel free to contact us.\n\n" +
                "Thank you for choosing EventTalk!\n" +
                "Best regards,\n" +
                "The EventTalk Team",
                booking.getUser().getUsername(),
                booking.getId(),
                booking.getEvents().getEventName(),
                booking.getTicketType(),
                booking.getPrice(),
                booking.getPaymentStatus(),
                status
            );

            helper.setText(emailContent);
            emailSender.send(message);
            System.out.println("Email sent successfully to " + booking.getUser().getEmail() + " for booking ID: " + booking.getId());
        } catch (MessagingException e) {
            System.out.println("Failed to send email for booking ID: " + booking.getId() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
