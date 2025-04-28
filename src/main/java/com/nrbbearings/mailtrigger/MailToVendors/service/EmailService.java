package com.nrbbearings.mailtrigger.MailToVendors.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.nrbbearings.mailtrigger.MailToVendors.controller.Contact;
import com.nrbbearings.mailtrigger.MailToVendors.exception.CustomException;

import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public List<Contact> extractContacts(InputStream inputStream) throws Exception {
        List<Contact> contacts = new ArrayList<>();

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        System.out.println(sheet.getLastRowNum());
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from row 1 (skip header)
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell nameCell = row.getCell(0); // Name in 1st column
                Cell emailCell = row.getCell(1); // Email in 2nd column

                if (nameCell != null && emailCell != null) {
                    String name = nameCell.getStringCellValue();
                    String email = emailCell.getStringCellValue();

                    if (isValidEmail(email)) {
                        contacts.add(new Contact(name, email));
                        System.out.println(name + "\t" + email);
                    } else {
                        throw new CustomException("Invalid email format: " + email);
                    }
                }
            }
        }
        //workbook.close();
        return contacts;
    
    }

    public void sendEmailWithAttachments(String to, String subject, String message, List<MultipartFile> attachments) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // true = multipart

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(message, false); // false means plain text (true = HTML)

        // Attach each file
        if (attachments != null) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));
                }
            }
        }

        mailSender.send(mimeMessage);
    }
    private boolean isValidEmail(String email) {
        // Simple regex for email validation
       return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}