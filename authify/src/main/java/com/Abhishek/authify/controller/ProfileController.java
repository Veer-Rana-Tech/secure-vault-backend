package com.Abhishek.authify.controller;


import com.Abhishek.authify.io.ProfileRequest;
import com.Abhishek.authify.io.ProfileResponse;
import com.Abhishek.authify.service.EmailService;
import com.Abhishek.authify.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {


    private final ProfileService profileService;
    private final EmailService emailService;



    @PostMapping("/register")
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response= profileService.createProfile(request);
        try {
            emailService.sendWelcomeEmail(response.getEmail(),response.getName());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to " + response.getEmail() + ": " + e.getMessage());
        }
        try {
            profileService.sendOtp(response.getEmail());
            System.out.println("Verification OTP sent to: " + response.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send verification OTP to " + response.getEmail() + ": " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/profile")
  public ProfileResponse getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return profileService.getProfile(email);
    }

}
