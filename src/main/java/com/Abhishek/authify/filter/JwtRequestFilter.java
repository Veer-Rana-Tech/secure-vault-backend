package com.Abhishek.authify.filter;


import com.Abhishek.authify.service.AppUsersDetailsService;
import com.Abhishek.authify.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    private final AppUsersDetailsService appUsersDetailsService;
    private final JwtUtil jwtUtil;

    private static final List<String> PUBLIC_URLS = List.of(
            "/login",
            "/register",
            "/send-otp",
            "/verify-otp",
            "/send-reset-otp",
            "/reset-password",
            "/logout"
    );

    public JwtRequestFilter(AppUsersDetailsService appUsersDetailsService, JwtUtil jwtUtil) {
        this.appUsersDetailsService = appUsersDetailsService;
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

       String path= request.getServletPath();
       if(PUBLIC_URLS.contains(path))
       {
           filterChain.doFilter(request,response);
           return;
       }

       String  jwt=null;
       String email=null;

       //1 authrization header
        final String authorizationHeader=request.getHeader("Authorization");
        if(authorizationHeader!=null &&authorizationHeader.startsWith("Bearer"))
        {
            jwt =authorizationHeader.substring(7);
        }

        // 2 if it not sounf in header ,check cookies
        if(jwt ==null)
        {
            Cookie[] cookies=request.getCookies();
            if (cookies !=null) {
                for(Cookie cookie:cookies)
                {
                    if ("jwt".equals(cookie.getName())) {
                        jwt=cookie.getValue();
                        break;
                    }
                }
            }
        }

        //3 validate the token and set the security context
        if (jwt!=null) {
            email=jwtUtil.extractEmail(jwt);
            if (email !=null&& SecurityContextHolder.getContext().getAuthentication()==null) {
               UserDetails userDetails= appUsersDetailsService.loadUserByUsername(email);
                if (jwtUtil.validateToken(jwt,userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }
        filterChain.doFilter(request,response);
    }



}
