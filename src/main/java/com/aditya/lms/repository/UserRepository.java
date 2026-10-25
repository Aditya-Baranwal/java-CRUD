package com.aditya.lms.repository;

import com.aditya.lms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIdIgnoreCase(String emailId);

    Optional<User> findByMobileNo(String mobileNo);

    boolean existsByEmailIdIgnoreCase(String emailId);

    boolean existsByEmailIdIgnoreCaseAndIdNot(String emailId, Long id);

    boolean existsByMobileNo(String mobileNo);

    boolean existsByMobileNoAndIdNot(String mobileNo, Long id);
}
