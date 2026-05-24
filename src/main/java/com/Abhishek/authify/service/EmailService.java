package com.Abhishek.authify.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
     private final JavaMailSender mailSender;

     @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;
     public void sendWelcomeEmail(String toEmail,String name) {
         try {
             SimpleMailMessage message= new SimpleMailMessage();
             message.setFrom(fromEmail);
             message.setTo(toEmail);
             message.setSubject("Welcome to Our Platform");
             message.setText("Hello " + name + ",\n\nThanks for registering with us!\n\nRegards,\nAuthify Team");
             mailSender.send(message);
             System.out.println("Welcome email sent successfully to: " + toEmail);
         } catch (Exception e) {
             System.err.println("Failed to send welcome email to " + toEmail + ": " + e.getMessage());
             e.printStackTrace();
             throw e; // Re-throw to let caller handle it
         }

     }

public void sendResetOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message=new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP");
            message.setText("Your otp for resetting your password is " + otp +". Use this OTP to proceed with resetting your password");
            mailSender.send(message);
            System.out.println("Reset OTP email sent successfully to: " + toEmail + ", OTP: " + otp);
        } catch (Exception e) {
            System.err.println("Failed to send reset OTP email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unable to send reset OTP email", e);
        }
    }

public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message=new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Account verification OTP");
            message.setText("Your OTP is  " + otp + ". Verify  your account using this OTP. ");
            mailSender.send(message);
            System.out.println("OTP email sent successfully to: " + toEmail + ", OTP: " + otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unable to send OTP email", e);
        }
    }

}
