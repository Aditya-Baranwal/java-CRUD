package com.aditya.lms.filter;


import com.aditya.lms.entity.User;
import com.aditya.lms.service.TokenService;
import com.aditya.lms.service.interfaces.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            Claims claims = tokenService.decodeToken(token);

            String userId = claims.getSubject();

            User currentUserDetails = userService.getUser(Long.parseLong(userId));

            List<String> values = claims.get("authorities", List.class);

            List<SimpleGrantedAuthority> authorities = values.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            currentUserDetails, token, authorities);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT");
            return;
        }

        chain.doFilter(request, response);
    }
}

