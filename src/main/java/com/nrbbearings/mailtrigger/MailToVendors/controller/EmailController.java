package com.nrbbearings.mailtrigger.MailToVendors.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
//import com.example.emailapp.model.EmailRequest;
//import com.example.emailapp.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nrbbearings.mailtrigger.MailToVendors.exception.CustomException;
import com.nrbbearings.mailtrigger.MailToVendors.service.EmailService;

@Controller
public class EmailController {

	@Autowired
	private EmailService emailService;
	
    @GetMapping("/")
    public String showUploadForm(Model model) {
        return "index"; // Return the Thymeleaf template
    }


@PostMapping("/send-emails")
public String sendEmails(@RequestParam("file") MultipartFile file, 
                         @RequestParam("subject") String subject, 
                         @RequestParam("message") String message, 
                         RedirectAttributes redirectAttributes) {
    try {
        List<Contact> contacts = emailService.extractContacts(file.getInputStream());
        for (Contact contact : contacts) {
            String personalizedMessage = message.replace("{name}", contact.getName());
            emailService.sendEmail(contact.getEmail(), subject, personalizedMessage);
        }
        redirectAttributes.addFlashAttribute("message", "Emails sent successfully!");
    } catch (CustomException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    } catch (IOException e) {
        redirectAttributes.addFlashAttribute("error", "Error reading the file: " + e.getMessage());
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
    }
    return "redirect:/";  
}

}
