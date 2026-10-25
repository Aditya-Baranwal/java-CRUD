package com.aditya.lms.service;

import com.aditya.lms.entity.User;
import com.aditya.lms.exception.ErrorMessages;
import com.aditya.lms.exception.UserConflictException;
import com.aditya.lms.exception.UserNotFoundException;
import com.aditya.lms.exception.UserValidationException;
import com.aditya.lms.repository.UserRepository;
import com.aditya.lms.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User createUser(User user) {
        normalizeUser(user);
        validateUserForCreate(user);
        validateUniqueFields(user.getEmailId(), user.getMobileNo(), null);

        User created = userRepository.save(user);
        log.info("User created successfully userId={}, emailId={}", created.getId(), created.getEmailId());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        if (userId == null) {
            throw new UserValidationException(ErrorMessages.USER_ID_MANDATORY);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String emailId) {
        String normalizedEmailId = normalizeEmail(emailId);
        if (normalizedEmailId == null) {
            throw new UserValidationException(ErrorMessages.USER_EMAIL_ID_MANDATORY);
        }
        return userRepository.findByEmailIdIgnoreCase(normalizedEmailId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.userNotFoundByEmail(normalizedEmailId)));
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User user) {
        if (userId == null) {
            throw new UserValidationException(ErrorMessages.USER_ID_MANDATORY);
        }
        if (user == null) {
            throw new UserValidationException(ErrorMessages.USER_PAYLOAD_REQUIRED);
        }

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        applyUpdates(existing, user);
        validateUserForUpdate(existing);
        validateUniqueFields(existing.getEmailId(), existing.getMobileNo(), existing.getId());

        User updated = userRepository.save(existing);
        log.info("User updated successfully userId={}", updated.getId());
        return updated;
    }

    /** Loads the user details based on the provided username (email or mobile number) */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedLoginId = normalizeLoginId(username);
        if (normalizedLoginId == null) {
            throw new UsernameNotFoundException(ErrorMessages.USER_EMAIL_ID_MANDATORY.message());
        }

        boolean isEmailLogin = normalizedLoginId.contains("@");
        Optional<User> userOptional = isEmailLogin
                ? userRepository.findByEmailIdIgnoreCase(normalizedLoginId)
                : userRepository.findByMobileNo(normalizedLoginId);

        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException(isEmailLogin
                ? ErrorMessages.userNotFoundByEmail(normalizedLoginId).message()
                : ErrorMessages.userNotFoundByMobileNo(normalizedLoginId).message()));

        return org.springframework.security.core.userdetails.User.withUsername(user.getEmailId())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }

    private void applyUpdates(User existing, User incoming) {
        if (incoming.getName() != null) {
            existing.setName(incoming.getName());
        }
        if (incoming.getDob() != null) {
            existing.setDob(incoming.getDob());
        }
        if (incoming.getMobileNo() != null) {
            existing.setMobileNo(incoming.getMobileNo());
        }
        if (incoming.getEmailId() != null) {
            existing.setEmailId(incoming.getEmailId());
        }
        if (incoming.getPasswordHash() != null) {
            existing.setPasswordHash(incoming.getPasswordHash());
        }
        if (incoming.getRole() != null) {
            existing.setRole(incoming.getRole());
        }
        if (incoming.getUpdatedBy() != null) {
            existing.setUpdatedBy(incoming.getUpdatedBy());
        }

        normalizeUser(existing);
    }

    private void validateUserForCreate(User user) {
        if (user == null) {
            throw new UserValidationException(ErrorMessages.USER_PAYLOAD_REQUIRED);
        }
        validateUserForUpdate(user);
    }

    private void validateUserForUpdate(User user) {
        if (user.getName() == null) {
            throw new UserValidationException(ErrorMessages.USER_NAME_MANDATORY);
        }
        if (user.getName().isBlank()) {
            throw new UserValidationException(ErrorMessages.USER_NAME_BLANK);
        }
        if (user.getDob() == null) {
            throw new UserValidationException(ErrorMessages.USER_DOB_MANDATORY);
        }
        if (user.getMobileNo() == null) {
            throw new UserValidationException(ErrorMessages.USER_MOBILE_NO_MANDATORY);
        }
        if (!user.getMobileNo().matches("\\d{10}")) {
            throw new UserValidationException(ErrorMessages.USER_MOBILE_NO_INVALID);
        }
        if (user.getEmailId() == null) {
            throw new UserValidationException(ErrorMessages.USER_EMAIL_ID_MANDATORY);
        }
        if (!user.getEmailId().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new UserValidationException(ErrorMessages.USER_EMAIL_ID_INVALID);
        }
        if (user.getPasswordHash() == null) {
            throw new UserValidationException(ErrorMessages.USER_PASSWORD_HASH_MANDATORY);
        }
        if (user.getPasswordHash().isBlank()) {
            throw new UserValidationException(ErrorMessages.USER_PASSWORD_HASH_BLANK);
        }
        if (user.getRole() == null) {
            throw new UserValidationException(ErrorMessages.USER_ROLE_MANDATORY);
        }
    }

    private void validateUniqueFields(String emailId, String mobileNo, Long userId) {
        boolean emailExists = userId == null
                ? userRepository.existsByEmailIdIgnoreCase(emailId)
                : userRepository.existsByEmailIdIgnoreCaseAndIdNot(emailId, userId);
        if (emailExists) {
            throw new UserConflictException(ErrorMessages.USER_DUPLICATE_EMAIL);
        }

        boolean mobileExists = userId == null
                ? userRepository.existsByMobileNo(mobileNo)
                : userRepository.existsByMobileNoAndIdNot(mobileNo, userId);
        if (mobileExists) {
            throw new UserConflictException(ErrorMessages.USER_DUPLICATE_MOBILE);
        }
    }

    private void normalizeUser(User user) {
        if (user == null) {
            return;
        }
        if (user.getName() != null) {
            user.setName(user.getName().trim());
        }
        user.setEmailId(normalizeEmail(user.getEmailId()));
        if (user.getMobileNo() != null) {
            user.setMobileNo(user.getMobileNo().trim());
        }
        if (user.getPasswordHash() != null) {
            user.setPasswordHash(user.getPasswordHash().trim());
        }
    }

    private String normalizeEmail(String emailId) {
        if (emailId == null) {
            return null;
        }
        String trimmed = emailId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase();
    }

    private String normalizeLoginId(String loginId) {
        if (loginId == null) {
            return null;
        }
        String trimmed = loginId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.contains("@")) {
            return trimmed.toLowerCase();
        }
        return trimmed;
    }
}
