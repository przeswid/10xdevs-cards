package com.ten.devs.cards.cards.user.application.command;

import com.ten.devs.cards.cards.user.domain.Role;
import com.ten.devs.cards.cards.user.domain.User;
import com.ten.devs.cards.cards.user.domain.UserRepository;
import com.ten.devs.cards.cards.user.presentation.response.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserCommandHandler")
class RegisterUserCommandHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserCommandHandler handler;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ENCODED_PASSWORD = "encodedPassword123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";

    @BeforeEach
    void setUp() {
        // Configure password encoder mock to return encoded password
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(TEST_ENCODED_PASSWORD);
    }

    @Test
    @DisplayName("Given valid registration data, When handling command, Then should create user with USER role")
    void givenValidRegistrationData_whenHandling_thenShouldCreateUserWithUserRole() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_PASSWORD,
            TEST_EMAIL,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );

        // Capture the user passed to save
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        RegisterResponse response = handler.handle(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isNotNull();

        // Verify save was called and capture the argument
        verify(userRepository, times(1)).save(userCaptor.capture());

        // Verify captured user has correct data
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.toSnapshot().username()).isEqualTo(TEST_USERNAME);
        assertThat(savedUser.toSnapshot().password()).isEqualTo(TEST_ENCODED_PASSWORD);
        assertThat(savedUser.toSnapshot().firstName()).isEqualTo(TEST_FIRST_NAME);
        assertThat(savedUser.toSnapshot().lastName()).isEqualTo(TEST_LAST_NAME);
        assertThat(savedUser.toSnapshot().email()).isEqualTo(TEST_EMAIL);
        assertThat(savedUser.toSnapshot().roles()).containsExactly(Role.USER);
    }

    @Test
    @DisplayName("Given valid command, When handling, Then should encode password using PasswordEncoder")
    void givenValidCommand_whenHandling_thenShouldEncodePassword() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_PASSWORD,
            TEST_EMAIL,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );


        // When
        handler.handle(command);

        // Then
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Given valid command, When handling, Then should return response with generated user ID")
    void givenValidCommand_whenHandling_thenShouldReturnResponseWithGeneratedId() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_PASSWORD,
            TEST_EMAIL,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );


        // When
        RegisterResponse response = handler.handle(command);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isNotNull();
    }

    @Test
    @DisplayName("Given username with special characters, When handling, Then should create user successfully")
    void givenUsernameWithSpecialCharacters_whenHandling_thenShouldCreateUserSuccessfully() {
        // Given
        String usernameWithSpecialChars = "user.name-123_test";
        RegisterUserCommand command = new RegisterUserCommand(
            usernameWithSpecialChars,
            TEST_PASSWORD,
            TEST_EMAIL,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        RegisterResponse response = handler.handle(command);

        // Then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.toSnapshot().username()).isEqualTo(usernameWithSpecialChars);
    }

    @Test
    @DisplayName("Given email in uppercase, When handling, Then should preserve email case")
    void givenEmailInUppercase_whenHandling_thenShouldPreserveEmailCase() {
        // Given
        String uppercaseEmail = "JOHN.DOE@EXAMPLE.COM";
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_PASSWORD,
            uppercaseEmail,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // When
        handler.handle(command);

        // Then
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.toSnapshot().email()).isEqualTo(uppercaseEmail);
    }

    @Test
    @DisplayName("Given multiple registration commands, When handling, Then should generate unique IDs")
    void givenMultipleRegistrationCommands_whenHandling_thenShouldGenerateUniqueIds() {
        // Given
        RegisterUserCommand command1 = new RegisterUserCommand(
            "user1",
            TEST_PASSWORD,
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            "user1@example.com"
        );

        RegisterUserCommand command2 = new RegisterUserCommand(
            "user2",
            TEST_PASSWORD,
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            "user2@example.com"
        );


        // When
        RegisterResponse response1 = handler.handle(command1);
        RegisterResponse response2 = handler.handle(command2);

        // Then
        assertThat(response1.userId()).isNotEqualTo(response2.userId());
    }

    @Test
    @DisplayName("Given repository throws exception, When handling, Then should propagate exception")
    void givenRepositoryThrowsException_whenHandling_thenShouldPropagateException() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_PASSWORD,
            TEST_EMAIL,
            TEST_FIRST_NAME,
            TEST_LAST_NAME
        );

        doThrow(new RuntimeException("Database connection error"))
            .when(userRepository).save(any(User.class));

        // When/Then
        assertThatThrownBy(() -> handler.handle(command))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database connection error");
    }
}
