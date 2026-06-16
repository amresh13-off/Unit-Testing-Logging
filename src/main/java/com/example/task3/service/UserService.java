package com.example.task3.service;

import com.example.task3.dto.CreateUserRequest;
import com.example.task3.dto.UpdateUserRequest;
import com.example.task3.dto.UserResponse;
import com.example.task3.entity.User;
import com.example.task3.entity.UserRole;
import com.example.task3.exception.DuplicateResourceException;
import com.example.task3.exception.UserNotFoundException;
import com.example.task3.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.debug("Attempting to create user with username: {}", request.getUsername());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate email attempted: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Duplicate username attempted: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User created successfully: id={}, username={}", saved.getId(), saved.getUsername());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException(id);
                });
        log.debug("User found: id={}, username={}", user.getId(), user.getUsername());
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Retrieved {} users from database", users.size());
        return users.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for update with id: {}", id);
                    return new UserNotFoundException(id);
                });

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                log.warn("Duplicate email during update: {}", request.getEmail());
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User saved = userRepository.save(user);
        log.info("User updated successfully: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.debug("Attempting to delete user with id: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("User not found for deletion with id: {}", id);
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: id={}", id);
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        log.debug("User found by email: {}", email);
        return toResponse(user);
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        log.debug("Activating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for activation with id: {}", id);
                    return new UserNotFoundException(id);
                });

        user.setActive(true);
        User saved = userRepository.save(user);
        log.info("User activated: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        log.debug("Deactivating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found for deactivation with id: {}", id);
                    return new UserNotFoundException(id);
                });

        user.setActive(false);
        User saved = userRepository.save(user);
        log.info("User deactivated: id={}", saved.getId());
        return toResponse(saved);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
