package com.EventManagement.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.EventManagement.model.Booking;
import com.EventManagement.model.Contact;
import com.EventManagement.model.Events;
import com.EventManagement.model.PaymentStatus;
import com.EventManagement.model.Review;
import com.EventManagement.model.User;
import com.EventManagement.repository.BookingRepository;
import com.EventManagement.repository.ContactRepository;
import com.EventManagement.repository.EventsRepository;
import com.EventManagement.repository.ReviewRepository;
import com.EventManagement.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
@Controller
public class HomeController {
	
	@Autowired
    private UserRepository userRepository;
	
	@Autowired
    private EventsRepository eventsRepository;
	
	@Autowired
    private ReviewRepository reviewRepository;
	
	@Autowired
    private ContactRepository contactRepository;
	
	@Autowired
    private BookingRepository bookingRepository;
	
    @Autowired
    private HttpSession session;
    
    @Autowired
    private JavaMailSender emailSender;
    
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
		model.addAttribute("contactForm", new Contact());
		String username = (String) session.getAttribute("username");
        if (username != null) {
            User user = userRepository.findByUsername(username);
            model.addAttribute("user", user);
            return "contact"; 
        }
        return "contact";
    }

	@PostMapping("/sendContactEmail")
	public String sendContactEmail(@ModelAttribute Contact contact, Model model) {
	    try {
	        MimeMessage message = emailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	
	        helper.setFrom("infoyarsatoursandtravels@gmail.com"); 
	        helper.setTo("infoyarsatoursandtravels@gmail.com");
	        helper.setCc(InternetAddress.parse(contact.getContactEmail()));
	        helper.setSubject("Contact Us Request from: " + contact.getContactName());
	        String content = "Name: " + contact.getContactName() + "\n" +
	                         "Email: " + contact.getContactEmail() + "\n" +
	                         "Subject: " + contact.getContactSubject() + "\n" +
	                         "Message: " + contact.getContactMessage();
	        helper.setText(content);
	
	     // Save contact in the database
	        Contact contactForm = new Contact();
	        contactForm.setContactName(contact.getContactName());
	        contactForm.setContactEmail(contact.getContactEmail());
	        contactForm.setContactSubject(contact.getContactSubject());
	        contactForm.setContactMessage(contact.getContactMessage());
	        contactRepository.save(contactForm);
	        
	        emailSender.send(message);
	        model.addAttribute("message", "Your message has been sent successfully!");
	    } catch (MessagingException e) {
	        model.addAttribute("error", "Error while sending email: " + e.getMessage());
	        e.printStackTrace();
	        return "redirect:/contactPage";
	    }
	
	    return "redirect:/contactPage";
	}
	
	@PostMapping("/submitReview")
	public String submitReview(@ModelAttribute("review") Review review, @RequestParam("eventId") int eventId, Model model) {
		Events events = eventsRepository.getById(eventId);
		review.setEvents(events);
        reviewRepository.save(review);
        return "redirect:/eventsDetails/" + eventId;
    }
	
	@PostMapping("/bookings/book")
	public String bookTicket(@RequestParam int eventId, @RequestParam String ticketType, HttpSession session, Model model) {

			String username = (String) session.getAttribute("username");
			
			if (username == null) {
			return "redirect:/loginPage"; 
			}
			
			User user = userRepository.findByUsername(username);

			Events events = eventsRepository.findById(eventId)
			       .orElseThrow(() -> new RuntimeException("Event not found"));

			Booking booking = new Booking();
			booking.setUser(user);
			booking.setEvents(events);
			booking.setTicketType(ticketType);
			booking.setPaymentStatus(PaymentStatus.PENDING);
			
			bookingRepository.save(booking);
			model.addAttribute("user", user);
			return "redirect:/bookings";
}
	
	@GetMapping("/bookings")
    public String getBookings(Model model, HttpSession session) {
		String username = (String) session.getAttribute("username");
		
		if (username == null) {
		return "redirect:/loginPage"; 
		}
		
		User user = userRepository.findByUsername(username);
        List<Booking> bookings = bookingRepository.findByUser(user);
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);
        return "bookings";
    }
	
	@GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable int id, Model model) {
		String username = (String) session.getAttribute("username");
		
		if (username == null) {
		return "redirect:/loginPage"; 
		}
		User user = userRepository.findByUsername(username);
        bookingRepository.deleteById(id);
        model.addAttribute("user", user);
        return "redirect:/bookings";
    }
	
	@GetMapping("/bookings/download/{id}")
    public void downloadTicketPdf(@PathVariable int id, HttpServletResponse response, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            try {
                response.sendRedirect("/loginPage");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            // Fetch booking details
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Check if the booking belongs to the current user
            if (!booking.getUser().getUsername().equals(username)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized access");
                return;
            }

            // Set response headers
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=ticket_" + id + ".pdf");

            // Create PDF document
            Document document = new Document();
            PdfWriter.getInstance(document, response.getOutputStream());
            
            document.open();
            
            // Add ticket details to PDF
            document.add(new Paragraph("Event Ticket"));
            document.add(new Paragraph("-------------------"));
            document.add(new Paragraph("Booking ID: " + booking.getId()));
            document.add(new Paragraph("Event: " + booking.getEvents().getEventName()));
            document.add(new Paragraph("Booked by: " + booking.getUser().getUsername()));
            
			
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            document.add(new Paragraph("Date: " + 
                booking.getEvents().getEventDate().format(dateFormatter)));

            // Format LocalTime
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            document.add(new Paragraph("Time: " + 
                booking.getEvents().getEventStartTime().format(timeFormatter) + " - " + 
                booking.getEvents().getEventEndTime().format(timeFormatter)));
            
            document.add(new Paragraph("Venue: " + booking.getEvents().getVenue()));
            document.add(new Paragraph("Ticket Type: " + booking.getTicketType()));
            document.add(new Paragraph("Payment Status: " + booking.getPaymentStatus()));
            document.add(new Paragraph("-------------------"));
            document.add(new Paragraph("Thank you for booking with EventTalk!"));

            document.close();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
	
	@GetMapping("/bookings/qrcode/{id}")
	public ResponseEntity<byte[]> generateQRCode(@PathVariable int id) throws IOException, WriterException {
	    Booking booking = bookingRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Booking not found"));

	    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

	    // Constructing detailed QR text
	    String qrText = String.format(
	        "Event Ticket\n" +
	        "-------------------\n" +
	        "Booking ID: %d\n" +
	        "Event: %s\n" +
	        "Booked by: %s\n" +
	        "Date: %s\n" +
	        "Time: %s - %s\n" +
	        "Venue: %s\n" +
	        "Ticket Type: %s\n" +
	        "Payment Status: %s\n" +
	        "-------------------\n" +
	        "Thank you for booking with EventTalk!",
	        booking.getId(),
	        booking.getEvents().getEventName(),
	        booking.getUser().getUsername(),
	        booking.getEvents().getEventDate().format(dateFormatter),
	        booking.getEvents().getEventStartTime().format(timeFormatter),
	        booking.getEvents().getEventEndTime().format(timeFormatter),
	        booking.getEvents().getVenue(),
	        booking.getTicketType(), 
	        booking.getPaymentStatus()
	    );

	    // Generate QR code using ZXing
	    QRCodeWriter qrCodeWriter = new QRCodeWriter();
	    BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, 250, 250);

	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);

	    return ResponseEntity.ok()
	            .contentType(MediaType.IMAGE_PNG)
	            .body(stream.toByteArray());
	}

	
}
