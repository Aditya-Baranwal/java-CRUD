package com.aditya.lms.controller;

import com.aditya.lms.entity.User;
import com.aditya.lms.enums.UserRole;
import com.aditya.lms.service.TokenService;
import com.aditya.lms.service.interfaces.UserService;
import com.aditya.lms.testdata.UserTestData;
import com.lms.model.AuthLoginRequestDTO;
import com.lms.model.AuthLoginResponseDTO;
import com.lms.model.AuthSignUpRequestDTO;
import com.lms.model.AuthSignUpRoleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldRegisterStudentUserOnSignUp() {
        AuthSignUpRequestDTO request = new AuthSignUpRequestDTO(
                "Aditya",
                LocalDate.parse("1998-01-15"),
                "9876543210",
                "aditya@example.com",
                "plain-password",
                AuthSignUpRoleDTO.STUDENT
        );
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        User created = UserTestData.persistedUser();
        when(userService.createUser(any(User.class))).thenReturn(created);

        var response = authController.signUp(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getUserId()).isEqualTo(1L);
        assertThat(response.getBody().getData().getUserRole()).isEqualTo(AuthSignUpRoleDTO.STUDENT);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
    }

    @Test
    void shouldLoginWithPhoneAndGenerateTokenUsingUserIdSubject() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO("9876543210", "plain-password");
        Authentication authentication = new UsernamePasswordAuthenticationToken("aditya@example.com", null, List.of());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        User user = UserTestData.persistedUser();
        when(userService.getUserByEmail("aditya@example.com")).thenReturn(user);
        when(tokenService.generateToken(eq("1"), eq(List.of("ROLE_USER")))).thenReturn("jwt-token");
        when(tokenService.getExpirySeconds()).thenReturn(3600L);

        var response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthLoginResponseDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().getToken()).isEqualTo("jwt-token");
        assertThat(body.getData().getUserId()).isEqualTo(1L);
        assertThat(body.getData().getAuthorities()).containsExactly("ROLE_USER");
        assertThat(body.getData().getExpiresInSeconds()).isEqualTo(3600L);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());
        assertThat(authCaptor.getValue().getPrincipal()).isEqualTo("9876543210");
        assertThat(authCaptor.getValue().getCredentials()).isEqualTo("plain-password");
    }

    @Test
    void shouldReturnUnauthorizedOnInvalidCredentials() {
        AuthLoginRequestDTO request = new AuthLoginRequestDTO("aditya@example.com", "bad-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        var response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void shouldLogoutSuccessfully() {
        var response = authController.logout();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Logout successful");
        assertThat(response.getBody().getData()).isEmpty();
    }
}
