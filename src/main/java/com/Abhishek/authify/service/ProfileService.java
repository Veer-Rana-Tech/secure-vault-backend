package com.Abhishek.authify.service;

import com.Abhishek.authify.io.ProfileRequest;
import com.Abhishek.authify.io.ProfileResponse;
import org.springframework.context.annotation.Profile;

public interface ProfileService {


    ProfileResponse createProfile(ProfileRequest  request);
   ProfileResponse getProfile(String email);

   void sendResetOtp(String email);

   void resetPassword(String email,String otp,String newPassword);
   void sendOtp(String email);
   void verifyOtp(String email,String otp);

   void changePassword(String email, String currentPassword, String newPassword);



}
