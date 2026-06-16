package com.example.task3.service;

import com.example.task3.dto.CreateUserRequest;
import com.example.task3.dto.UpdateUserRequest;
import com.example.task3.dto.UserResponse;
import com.example.task3.entity.User;
import com.example.task3.entity.UserRole;
import com.example.task3.exception.DuplicateResourceException;
import com.example.task3.exception.UserNotFoundException;
import com.example.task3.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User createTestUser(Long id, String username, String email, boolean active) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .active(active)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void createUser_Success() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("johndoe")
                    .email("john@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(1L);
                u.setCreatedAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                return u;
            });

            UserResponse response = userService.createUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getRole()).isEqualTo(UserRole.USER);
            assertThat(response.getActive()).isTrue();

            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).existsByUsername("johndoe");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void createUser_DuplicateEmail_ThrowsException() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("johndoe")
                    .email("existing@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository).existsByEmail("existing@example.com");
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should throw exception when username already exists")
        void createUser_DuplicateUsername_ThrowsException() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("existinguser")
                    .email("john@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Username already exists");

            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).existsByUsername("existinguser");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("should assign default USER role when role is null")
        void createUser_DefaultRole_WhenRoleIsNull() {
            CreateUserRequest request = CreateUserRequest.builder()
                    .username("janedoe")
                    .email("jane@example.com")
                    .firstName("Jane")
                    .lastName("Doe")
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                u.setCreatedAt(LocalDateTime.now());
                u.setUpdatedAt(LocalDateTime.now());
                return u;
            });

            UserResponse response = userService.createUser(request);

            assertThat(response.getRole()).isEqualTo(UserRole.USER);
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void getUserById_Success() {
            User user = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponse response = userService.getUserById(1L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getActive()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void getUserById_NotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("should return list of users")
        void getAllUsers_ReturnsList() {
            List<User> users = List.of(
                    createTestUser(1L, "john", "john@example.com", true),
                    createTestUser(2L, "jane", "jane@example.com", true)
            );
            when(userRepository.findAll()).thenReturn(users);

            List<UserResponse> responses = userService.getAllUsers();

            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(UserResponse::getUsername)
                    .containsExactly("john", "jane");
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void getAllUsers_EmptyList_ReturnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponse> responses = userService.getAllUsers();

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("should update user fields successfully")
        void updateUser_Success() {
            User existing = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("Jonathan")
                    .lastName("Smith")
                    .role(UserRole.ADMIN)
                    .build();

            UserResponse response = userService.updateUser(1L, request);

            assertThat(response.getFirstName()).isEqualTo("Jonathan");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(response.getUsername()).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("should throw exception when user not found for update")
        void updateUser_NotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("Ghost")
                    .build();

            assertThatThrownBy(() -> userService.updateUser(99L, request))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should apply partial update correctly")
        void updateUser_PartialUpdate() {
            User existing = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .firstName("NewName")
                    .build();

            UserResponse response = userService.updateUser(1L, request);

            assertThat(response.getFirstName()).isEqualTo("NewName");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should throw exception on duplicate email during update")
        void updateUser_DuplicateEmail_ThrowsException() {
            User existing = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            UpdateUserRequest request = UpdateUserRequest.builder()
                    .email("taken@example.com")
                    .build();

            assertThatThrownBy(() -> userService.updateUser(1L, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Email already exists");
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void deleteUser_Success() {
            when(userRepository.existsById(1L)).thenReturn(true);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw exception when user not found for deletion")
        void deleteUser_NotFound_ThrowsException() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");

            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email exists")
        void findByEmail_Success() {
            User user = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

            UserResponse response = userService.findByEmail("john@example.com");

            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("should throw exception when email not found")
        void findByEmail_NotFound_ThrowsException() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findByEmail("unknown@example.com"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    @DisplayName("activateUser() / deactivateUser()")
    class Activation {

        @Test
        @DisplayName("should activate a deactivated user")
        void activateUser_Success() {
            User user = createTestUser(1L, "johndoe", "john@example.com", false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.activateUser(1L);

            assertThat(response.getActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate an active user")
        void deactivateUser_Success() {
            User user = createTestUser(1L, "johndoe", "john@example.com", true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.deactivateUser(1L);

            assertThat(response.getActive()).isFalse();
        }

        @Test
        @DisplayName("should throw exception when activating non-existent user")
        void activateUser_NotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.activateUser(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should throw exception when deactivating non-existent user")
        void deactivateUser_NotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deactivateUser(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }
}
