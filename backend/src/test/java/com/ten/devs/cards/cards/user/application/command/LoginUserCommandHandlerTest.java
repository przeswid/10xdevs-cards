package com.ten.devs.cards.cards.user.application.command;

import com.ten.devs.cards.cards.user.domain.Role;
import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserId;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import com.ten.devs.cards.cards.user.presentation.response.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUserCommandHandler")
class LoginUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtOperations jwtOperations;

    @InjectMocks
    private LoginUserCommandHandler handler;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ENCODED_PASSWORD = "encodedPassword123";
    private static final String TEST_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final long TEST_EXPIRATION_TIME = 3600000L; // 1 hour

    private User createTestUser() {
        return User.newUser(
            UserId.random(),
            TEST_USERNAME,
            TEST_ENCODED_PASSWORD,
            "John",
            "Doe",
            "john.doe@example.com",
            List.of(Role.USER)
        );
    }

    @Nested
    @DisplayName("Successful authentication")
    class SuccessfulAuthentication {

        @Test
        @DisplayName("Given valid credentials, When handling login, Then should return JWT token")
        void givenValidCredentials_whenHandlingLogin_thenShouldReturnJwtToken() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = createTestUser();

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            LoginResponse response = handler.handle(command);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.username()).isEqualTo(TEST_USERNAME);
            assertThat(response.accessToken()).isEqualTo(TEST_JWT_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(TEST_EXPIRATION_TIME);
        }

        @Test
        @DisplayName("Given valid credentials, When handling login, Then should authenticate with AuthenticationManager")
        void givenValidCredentials_whenHandlingLogin_thenShouldAuthenticateWithManager() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = createTestUser();

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            handler.handle(command);

            // Then
            verify(authenticationManager, times(1)).authenticate(
                argThat(auth ->
                    auth instanceof UsernamePasswordAuthenticationToken &&
                    auth.getPrincipal().equals(TEST_USERNAME) &&
                    auth.getCredentials().equals(TEST_PASSWORD)
                )
            );
        }

        @Test
        @DisplayName("Given valid credentials, When handling login, Then should load user details from repository")
        void givenValidCredentials_whenHandlingLogin_thenShouldLoadUserDetailsFromRepository() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = createTestUser();

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            handler.handle(command);

            // Then
            verify(userRepository, times(1)).getUserByUsername(TEST_USERNAME);
        }

        @Test
        @DisplayName("Given valid credentials, When handling login, Then should generate JWT with user details")
        void givenValidCredentials_whenHandlingLogin_thenShouldGenerateJwtWithUserDetails() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = createTestUser();

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            handler.handle(command);

            // Then
            verify(jwtOperations, times(1)).generateToken(
                argThat(userDetails ->
                    userDetails.getUsername().equals(TEST_USERNAME) &&
                    userDetails.getPassword().equals(TEST_ENCODED_PASSWORD) &&
                    userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("USER"))
                )
            );
        }

        @Test
        @DisplayName("Given user with multiple roles, When handling login, Then should include all roles in UserDetails")
        void givenUserWithMultipleRoles_whenHandlingLogin_thenShouldIncludeAllRolesInUserDetails() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = User.newUser(
                UserId.random(),
                TEST_USERNAME,
                TEST_ENCODED_PASSWORD,
                "John",
                "Doe",
                "john.doe@example.com",
                List.of(Role.USER, Role.ADMIN)
            );

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            handler.handle(command);

            // Then
            verify(jwtOperations, times(1)).generateToken(
                argThat(userDetails ->
                    userDetails.getAuthorities().size() == 2 &&
                    userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("USER")) &&
                    userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ADMIN"))
                )
            );
        }
    }

    @Nested
    @DisplayName("Authentication failures")
    class AuthenticationFailures {

        @Test
        @DisplayName("Given invalid credentials, When handling login, Then should throw BadCredentialsException")
        void givenInvalidCredentials_whenHandlingLogin_thenShouldThrowBadCredentialsException() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Bad credentials");

            // Verify getUserByUsername was never called
            verify(userRepository, never()).getUserByUsername(any());
        }

        @Test
        @DisplayName("Given non-existent username, When handling login, Then should throw RuntimeException")
        void givenNonExistentUsername_whenHandlingLogin_thenShouldThrowRuntimeException() {
            // Given
            LoginUserCommand command = new LoginUserCommand("nonexistent", TEST_PASSWORD);

            when(userRepository.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found: nonexistent");
        }

        @Test
        @DisplayName("Given authentication succeeds but user not in repository, When handling login, Then should throw RuntimeException")
        void givenAuthenticationSucceedsButUserNotInRepository_whenHandlingLogin_thenShouldThrowRuntimeException() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);

            // AuthenticationManager succeeds but user not found in repository
            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found: " + TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Given username with special characters, When handling login, Then should authenticate successfully")
        void givenUsernameWithSpecialCharacters_whenHandlingLogin_thenShouldAuthenticateSuccessfully() {
            // Given
            String specialUsername = "user.name-123_test";
            LoginUserCommand command = new LoginUserCommand(specialUsername, TEST_PASSWORD);
            User user = User.newUser(
                UserId.random(),
                specialUsername,
                TEST_ENCODED_PASSWORD,
                "John",
                "Doe",
                "john.doe@example.com",
                List.of(Role.USER)
            );

            when(userRepository.getUserByUsername(specialUsername)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class))).thenReturn(TEST_JWT_TOKEN);
            when(jwtOperations.getExpirationTime()).thenReturn(TEST_EXPIRATION_TIME);

            // When
            LoginResponse response = handler.handle(command);

            // Then
            assertThat(response.username()).isEqualTo(specialUsername);
        }

        @Test
        @DisplayName("Given repository throws exception, When handling login, Then should propagate exception")
        void givenRepositoryThrowsException_whenHandlingLogin_thenShouldPropagateException() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);

            when(userRepository.getUserByUsername(TEST_USERNAME))
                .thenThrow(new RuntimeException("Database connection error"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection error");
        }

        @Test
        @DisplayName("Given JwtOperations throws exception, When handling login, Then should propagate exception")
        void givenJwtOperationsThrowsException_whenHandlingLogin_thenShouldPropagateException() {
            // Given
            LoginUserCommand command = new LoginUserCommand(TEST_USERNAME, TEST_PASSWORD);
            User user = createTestUser();

            when(userRepository.getUserByUsername(TEST_USERNAME)).thenReturn(Optional.of(user));
            when(jwtOperations.generateToken(any(UserDetails.class)))
                .thenThrow(new RuntimeException("JWT generation failed"));

            // When/Then
            assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("JWT generation failed");
        }
    }
}
