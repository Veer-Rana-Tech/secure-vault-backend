package com.Abhishek.authify.service;


import com.Abhishek.authify.entity.UserEntity;
import com.Abhishek.authify.io.ProfileRequest;
import com.Abhishek.authify.io.ProfileResponse;
import com.Abhishek.authify.repository.UserRepostory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepostory userRepostory;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity newProfile= convertToUserEntity(request);
        if(!userRepostory.existsByEmail((request.getEmail())))
        {
            newProfile = userRepostory.save(newProfile);
            return convertToProfileResponse(newProfile);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already exists");



    }

    @Override
    public ProfileResponse getProfile(String email) {
       UserEntity existingUser= userRepostory.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("user not found" + email));
       return convertToProfileResponse(existingUser);

    }

    @Override
    public void sendResetOtp(String email) {
      UserEntity existingEntity=  userRepostory.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found: "+ email));

        //genertate 6 digit otp
       String otp= String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
        // calculate the expiry time(current time+15 mins in millisecondd)
       Long expiryTime= System.currentTimeMillis() +(15*60*1000);

       // update profile entity/user
        existingEntity.setResetOtp(otp);
        existingEntity.setResetOtpExpireAt(expiryTime);

        //save into databse
        userRepostory.save(existingEntity);

        try{
            //Todo : send the reset otp email
            emailService.sendResetOtpEmail(existingEntity.getEmail(),otp);
        }
        catch (Exception ex) {
            throw new RuntimeException("Unable to send email");
        }

    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
      UserEntity existingUser= userRepostory.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found : " + email));
      if(existingUser.getResetOtp()==null || !existingUser.getResetOtp().equals(otp)) {
          throw new RuntimeException("Invalid OTP");
      }

      if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis())
      {
          throw new RuntimeException("OTP Expired");
      }
      existingUser.setPassword(passwordEncoder.encode(newPassword));
      existingUser.setResetOtp(null);
      existingUser.setResetOtpExpireAt(0L);


      userRepostory.save(existingUser);

    }

@Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRepostory.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) {
            System.out.println("Account already verified for email: " + email);
            return;
        }
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        Long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expiryTime);

        userRepostory.save(existingUser);
        System.out.println("OTP generated: " + otp + " for email: " + email);
        try {
            emailService.sendOtpEmail(existingUser.getEmail(), otp);
        } catch (Exception e) {
            System.err.println("Failed to send OTP email to " + existingUser.getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Unable to Send Email", e);
        }

    }

    @Override
    public void verifyOtp(String email, String otp) {
      UserEntity existingUser=userRepostory.findByEmail(email)
        .orElseThrow(()->new UsernameNotFoundException("User not found: "+ email));
      if (existingUser.getVerifyOtp()==null || !existingUser.getVerifyOtp().equals(otp)) {
          throw new RuntimeException("Invalid OTP");
      }

      if(existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis()) {
          throw new RuntimeException("OTP Expired");
      }
       existingUser.setIsAccountVerified(true);
      existingUser.setVerifyOtp(null);
      existingUser.setVerifyOtpExpireAt(0L);

      userRepostory.save(existingUser);



}

@Override
public void changePassword(String email, String currentPassword, String newPassword) {
    UserEntity existingUser = userRepostory.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

    // Verify current password
    if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
        throw new RuntimeException("Current password is incorrect");
    }

    // Update password
    existingUser.setPassword(passwordEncoder.encode(newPassword));
    userRepostory.save(existingUser);
}


private ProfileResponse convertToProfileResponse(UserEntity newProfile) {
        return ProfileResponse.builder()
                .name(newProfile.getName())
                .email(newProfile.getEmail())
                .userId(newProfile.getUserId())
                .isAccountVerified(newProfile.getIsAccountVerified())
                .createdAt(newProfile.getCreatedAt())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return  UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }


}
