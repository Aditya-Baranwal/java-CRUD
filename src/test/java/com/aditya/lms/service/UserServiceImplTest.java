package com.aditya.lms.service;

import com.aditya.lms.entity.User;
import com.aditya.lms.enums.UserRole;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.UserConflictException;
import com.aditya.lms.exception.UserNotFoundException;
import com.aditya.lms.exception.UserValidationException;
import com.aditya.lms.repository.UserRepository;
import com.aditya.lms.testdata.UserTestData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Nested
    class CreateUser {

        @Test
        void shouldCreateUserWhenPayloadIsValid() {
            User newUser = UserTestData.newUnsavedUser();
            when(userRepository.existsByEmailIdIgnoreCase("aditya@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNo("9876543210")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User created = userService.createUser(newUser);

            assertThat(created.getEmailId()).isEqualTo("aditya@example.com");
            assertThat(created.getMobileNo()).isEqualTo("9876543210");
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getName()).isEqualTo("Aditya Baranwal");
        }

        @Test
        void shouldNormalizeEmailToLowerCaseBeforeSaving() {
            User newUser = UserTestData.newUnsavedUser();
            newUser.setEmailId("  Aditya@Example.COM ");
            when(userRepository.existsByEmailIdIgnoreCase("aditya@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNo("9876543210")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User created = userService.createUser(newUser);

            assertThat(created.getEmailId()).isEqualTo("aditya@example.com");
        }

        @Test
        void shouldThrowWhenPayloadIsNull() {
            assertThatThrownBy(() -> userService.createUser(null))
                    .isInstanceOf(UserValidationException.class)
                    .hasMessage(ErrorMessages.USER_PAYLOAD_REQUIRED.message());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenRoleIsMissing() {
            User newUser = UserTestData.newUnsavedUser();
            newUser.setRole(null);

            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(UserValidationException.class)
                    .hasMessage(ErrorMessages.USER_ROLE_MANDATORY.message());
        }

        @Test
        void shouldThrowConflictWhenEmailAlreadyExists() {
            User newUser = UserTestData.newUnsavedUser();
            when(userRepository.existsByEmailIdIgnoreCase("aditya@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(UserConflictException.class)
                    .hasMessage(ErrorMessages.USER_DUPLICATE_EMAIL.message());
            verify(userRepository, never()).save(any());
        }

        @Test
        void shouldThrowConflictWhenMobileAlreadyExists() {
            User newUser = UserTestData.newUnsavedUser();
            when(userRepository.existsByEmailIdIgnoreCase("aditya@example.com")).thenReturn(false);
            when(userRepository.existsByMobileNo("9876543210")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(newUser))
                    .isInstanceOf(UserConflictException.class)
                    .hasMessage(ErrorMessages.USER_DUPLICATE_MOBILE.message());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class GetUser {

        @Test
        void shouldReturnUserWhenFound() {
            User existingUser = UserTestData.persistedUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            User result = userService.getUser(1L);

            assertThat(result).isEqualTo(existingUser);
        }

        @Test
        void shouldThrowNotFoundWhenUserMissing() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessage(ErrorMessages.userNotFound(99L).message());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void shouldUpdateMutableFields() {
            User existingUser = UserTestData.persistedUser();
            User incoming = User.builder()
                    .name("Updated User")
                    .dob(existingUser.getDob().plusDays(1))
                    .mobileNo("9123456789")
                    .emailId("updated@example.com")
                    .passwordHash("updated-hash")
                    .role(UserRole.ADMIN)
                    .updatedBy(5L)
                    .build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmailIdIgnoreCaseAndIdNot("updated@example.com", 1L)).thenReturn(false);
            when(userRepository.existsByMobileNoAndIdNot("9123456789", 1L)).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User updated = userService.updateUser(1L, incoming);

            assertThat(updated.getName()).isEqualTo("Updated User");
            assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(updated.getUpdatedBy()).isEqualTo(5L);
        }
    }

    @Nested
    class LoadUserByUsername {

        @Test
        void shouldLoadSpringSecurityUserByEmail() {
            User existingUser = UserTestData.persistedUser();
            when(userRepository.findByEmailIdIgnoreCase("aditya@example.com")).thenReturn(Optional.of(existingUser));

            UserDetails userDetails = userService.loadUserByUsername("Aditya@Example.COM");

            assertThat(userDetails.getUsername()).isEqualTo("aditya@example.com");
            assertThat(userDetails.getPassword()).isEqualTo(existingUser.getPasswordHash());
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        void shouldThrowUsernameNotFoundWhenEmailDoesNotExist() {
            when(userRepository.findByEmailIdIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loadUserByUsername("missing@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(ErrorMessages.userNotFoundByEmail("missing@example.com").message());
        }

        @Test
        void shouldLoadSpringSecurityUserByMobileNo() {
            User existingUser = UserTestData.persistedUser();
            when(userRepository.findByMobileNo("9876543210")).thenReturn(Optional.of(existingUser));

            UserDetails userDetails = userService.loadUserByUsername(" 9876543210 ");

            assertThat(userDetails.getUsername()).isEqualTo("aditya@example.com");
            assertThat(userDetails.getPassword()).isEqualTo(existingUser.getPasswordHash());
            assertThat(userDetails.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        void shouldThrowUsernameNotFoundWhenMobileNoDoesNotExist() {
            when(userRepository.findByMobileNo("9999999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.loadUserByUsername("9999999999"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage(ErrorMessages.userNotFoundByMobileNo("9999999999").message());
        }
    }
}
