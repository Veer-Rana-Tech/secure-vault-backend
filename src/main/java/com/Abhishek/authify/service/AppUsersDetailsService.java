package com.Abhishek.authify.service;

import com.Abhishek.authify.entity.UserEntity;
import com.Abhishek.authify.repository.UserRepostory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AppUsersDetailsService implements UserDetailsService {



    private final UserRepostory userRepostory;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity existingUser= userRepostory.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Email not found for the email : " + email));
        return new User(existingUser.getEmail(),existingUser.getPassword(),new ArrayList<>());
    }
}
