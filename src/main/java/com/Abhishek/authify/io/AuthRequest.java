package com.Abhishek.authify.io;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@NotNull
@Builder
public class AuthRequest {

    private String  email;
    private String password;
}
