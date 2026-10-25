package com.aditya.lms.controller;

import com.aditya.lms.entity.User;
import com.aditya.lms.enums.UserRole;
import com.aditya.lms.service.TokenService;
import com.aditya.lms.service.interfaces.UserService;
import com.lms.api.AuthApi;
import com.lms.model.AuthLoginPayloadDTO;
import com.lms.model.AuthLoginRequestDTO;
import com.lms.model.AuthLoginResponseDTO;
import com.lms.model.AuthLogoutResponseDTO;
import com.lms.model.AuthSignUpRequestDTO;
import com.lms.model.AuthSignUpResponseDTO;
import com.lms.model.AuthSignUpRoleDTO;
import com.lms.model.AuthUserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    public ResponseEntity<AuthSignUpResponseDTO> signUp(@Valid AuthSignUpRequestDTO authSignUpRequestDTO) {
        UserRole role = toDomainRole(authSignUpRequestDTO.getUserRole());

        User user = User.builder()
                .name(authSignUpRequestDTO.getName())
                .dob(authSignUpRequestDTO.getDob())
                .mobileNo(authSignUpRequestDTO.getMobileNo())
                .emailId(authSignUpRequestDTO.getEmailId())
                .passwordHash(passwordEncoder.encode(authSignUpRequestDTO.getPassword()))
                .role(role)
                .build();

        User createdUser = userService.createUser(user);

        AuthUserResponseDTO payload = new AuthUserResponseDTO()
                .userId(createdUser.getId())
                .emailId(createdUser.getEmailId())
                .mobileNo(createdUser.getMobileNo())
                .userRole(toSignUpRole(createdUser.getRole()));

        AuthSignUpResponseDTO response = new AuthSignUpResponseDTO()
                .message("User registered successfully")
                .data(payload)
                .timestamp(OffsetDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<AuthLoginResponseDTO> login(@Valid AuthLoginRequestDTO authLoginRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authLoginRequestDTO.getLoginId(),
                            authLoginRequestDTO.getPassword()
                    )
            );

            User user = userService.getUserByEmail(authentication.getName());
            List<String> authorities = List.of("ROLE_" + user.getRole().name());
            String token = tokenService.generateToken(String.valueOf(user.getId()), authorities);

            AuthLoginPayloadDTO payload = new AuthLoginPayloadDTO()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresInSeconds(tokenService.getExpirySeconds())
                    .userId(user.getId())
                    .authorities(authorities);

            AuthLoginResponseDTO response = new AuthLoginResponseDTO()
                    .message("Login successful")
                    .data(payload)
                    .timestamp(OffsetDateTime.now());

            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            AuthLoginResponseDTO response = new AuthLoginResponseDTO()
                    .message("Invalid credentials")
                    .timestamp(OffsetDateTime.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @Override
    public ResponseEntity<AuthLogoutResponseDTO> logout() {
        /** BAD IMPLEMENTATION */
        SecurityContextHolder.clearContext();
        AuthLogoutResponseDTO response = new AuthLogoutResponseDTO()
                .message("Logout successful")
                .timestamp(OffsetDateTime.now());
        return ResponseEntity.ok(response);
    }

    private UserRole toDomainRole(AuthSignUpRoleDTO role) {
        if (role == AuthSignUpRoleDTO.INSTRUCTOR) {
            return UserRole.INSTRUCTOR;
        }
        return UserRole.STUDENT;
    }

    private AuthSignUpRoleDTO toSignUpRole(UserRole role) {
        if (role == UserRole.INSTRUCTOR) {
            return AuthSignUpRoleDTO.INSTRUCTOR;
        }
        return AuthSignUpRoleDTO.STUDENT;
    }
}
