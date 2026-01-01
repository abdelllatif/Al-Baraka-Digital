package com.albaraka.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/agent/operations/pending");
    }

    @Override
    protected void  doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
             final String autorizationToken = request.getHeader("Authorization");
             String username = null;
             String jwt = null;
             if(autorizationToken !=null && autorizationToken.startsWith("Bearer ")){
                 jwt = autorizationToken.substring(7);
                 username= jwtUtil.extractUsername(jwt);
                 if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                     UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                     if(jwtUtil.validateToken(jwt,userDetails)) {
                         new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                         SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
                     }
                 }
             }
        filterChain.doFilter(request, response);
    }
}
