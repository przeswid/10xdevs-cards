package com.ten.devs.cards.cards.user.application.command;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtOperations")
class JwtOperationsTest {

    private JwtOperations jwtOperations;
    private static final String TEST_SECRET_KEY = "dGVzdC1zZWNyZXQta2V5LXRoYXQtbmVlZHMtdG8tYmUtYXQtbGVhc3QtMjU2LWJpdHM="; // Base64 encoded
    private static final long TEST_EXPIRATION = 3600000L; // 1 hour
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtOperations = new JwtOperations();
        ReflectionTestUtils.setField(jwtOperations, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtOperations, "jwtExpiration", TEST_EXPIRATION);
    }

    private UserDetails createTestUserDetails() {
        return User.builder()
            .username(TEST_USERNAME)
            .password("password")
            .authorities("USER")
            .build();
    }

    @Nested
    @DisplayName("Token generation")
    class TokenGeneration {

        @Test
        @DisplayName("Given user details, When generating token, Then should return valid JWT token")
        void givenUserDetails_whenGeneratingToken_thenShouldReturnValidJwtToken() {
            // Given
            UserDetails userDetails = createTestUserDetails();

            // When
            String token = jwtOperations.generateToken(userDetails);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
        }

        @Test
        @DisplayName("Given user details with extra claims, When generating token, Then should include extra claims")
        void givenUserDetailsWithExtraClaims_whenGeneratingToken_thenShouldIncludeExtraClaims() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("customClaim", "customValue");
            extraClaims.put("role", "ADMIN");

            // When
            String token = jwtOperations.generateToken(extraClaims, userDetails);

            // Then
            assertThat(token).isNotNull();
            String customClaimValue = jwtOperations.extractClaim(token, claims -> claims.get("customClaim", String.class));
            String roleValue = jwtOperations.extractClaim(token, claims -> claims.get("role", String.class));

            assertThat(customClaimValue).isEqualTo("customValue");
            assertThat(roleValue).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Given same user details, When generating token, Then should include issued-at timestamp")
        void givenSameUserDetails_whenGeneratingToken_thenShouldIncludeIssuedAtTimestamp() {
            // Given
            UserDetails userDetails = createTestUserDetails();

            // When
            String token = jwtOperations.generateToken(userDetails);
            Date issuedAt = jwtOperations.extractClaim(token, Claims::getIssuedAt);

            // Then
            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt).isBeforeOrEqualTo(new Date()); // Issued at or before now
        }
    }

    @Nested
    @DisplayName("Token extraction")
    class TokenExtraction {

        @Test
        @DisplayName("Given valid token, When extracting username, Then should return correct username")
        void givenValidToken_whenExtractingUsername_thenShouldReturnCorrectUsername() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            String token = jwtOperations.generateToken(userDetails);

            // When
            String extractedUsername = jwtOperations.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Given valid token, When extracting claims with function, Then should return correct claim value")
        void givenValidToken_whenExtractingClaimsWithFunction_thenShouldReturnCorrectClaimValue() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            String token = jwtOperations.generateToken(userDetails);

            // When
            String username = jwtOperations.extractClaim(token, Claims::getSubject);
            Date issuedAt = jwtOperations.extractClaim(token, Claims::getIssuedAt);
            Date expiration = jwtOperations.extractClaim(token, Claims::getExpiration);

            // Then
            assertThat(username).isEqualTo(TEST_USERNAME);
            assertThat(issuedAt).isNotNull();
            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(issuedAt);
        }

        @Test
        @DisplayName("Given token with custom claims, When extracting custom claim, Then should return correct value")
        void givenTokenWithCustomClaims_whenExtractingCustomClaim_thenShouldReturnCorrectValue() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("department", "Engineering");
            String token = jwtOperations.generateToken(extraClaims, userDetails);

            // When
            String department = jwtOperations.extractClaim(token, claims -> claims.get("department", String.class));

            // Then
            assertThat(department).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("Given malformed token, When extracting username, Then should throw MalformedJwtException")
        void givenMalformedToken_whenExtractingUsername_thenShouldThrowMalformedJwtException() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When/Then
            assertThatThrownBy(() -> jwtOperations.extractUsername(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Given empty token, When extracting username, Then should throw IllegalArgumentException")
        void givenEmptyToken_whenExtractingUsername_thenShouldThrowIllegalArgumentException() {
            // When/Then
            assertThatThrownBy(() -> jwtOperations.extractUsername(""))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Token validation")
    class TokenValidation {

        @Test
        @DisplayName("Given valid token for user, When validating, Then should return true")
        void givenValidTokenForUser_whenValidating_thenShouldReturnTrue() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            String token = jwtOperations.generateToken(userDetails);

            // When
            boolean isValid = jwtOperations.isTokenValid(token, userDetails);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Given token for different user, When validating, Then should return false")
        void givenTokenForDifferentUser_whenValidating_thenShouldReturnFalse() {
            // Given
            UserDetails originalUser = createTestUserDetails();
            String token = jwtOperations.generateToken(originalUser);

            UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities("USER")
                .build();

            // When
            boolean isValid = jwtOperations.isTokenValid(token, differentUser);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Given expired token, When validating, Then should return false")
        void givenExpiredToken_whenValidating_thenShouldReturnFalse() {
            // Given - create JwtOperations with very short expiration
            JwtOperations shortLivedJwtOps = new JwtOperations();
            ReflectionTestUtils.setField(shortLivedJwtOps, "secretKey", TEST_SECRET_KEY);
            ReflectionTestUtils.setField(shortLivedJwtOps, "jwtExpiration", 1L); // 1ms

            UserDetails userDetails = createTestUserDetails();
            String token = shortLivedJwtOps.generateToken(userDetails);

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When/Then
            assertThatThrownBy(() -> shortLivedJwtOps.isTokenValid(token, userDetails))
                .isInstanceOf(ExpiredJwtException.class);
        }

        @Test
        @DisplayName("Given malformed token, When validating, Then should throw MalformedJwtException")
        void givenMalformedToken_whenValidating_thenShouldThrowMalformedJwtException() {
            // Given
            String malformedToken = "invalid.jwt.token";
            UserDetails userDetails = createTestUserDetails();

            // When/Then
            assertThatThrownBy(() -> jwtOperations.isTokenValid(malformedToken, userDetails))
                .isInstanceOf(MalformedJwtException.class);
        }
    }

    @Nested
    @DisplayName("Expiration time")
    class ExpirationTime {

        @Test
        @DisplayName("When getting expiration time, Then should return configured value")
        void whenGettingExpirationTime_thenShouldReturnConfiguredValue() {
            // When
            long expirationTime = jwtOperations.getExpirationTime();

            // Then
            assertThat(expirationTime).isEqualTo(TEST_EXPIRATION);
        }

        @Test
        @DisplayName("Given token, When checking expiration, Then token should expire after configured time")
        void givenToken_whenCheckingExpiration_thenTokenShouldExpireAfterConfiguredTime() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            String token = jwtOperations.generateToken(userDetails);

            // When
            Date issuedAt = jwtOperations.extractClaim(token, Claims::getIssuedAt);
            Date expiration = jwtOperations.extractClaim(token, Claims::getExpiration);

            // Then
            long actualExpirationDuration = expiration.getTime() - issuedAt.getTime();
            assertThat(actualExpirationDuration).isEqualTo(TEST_EXPIRATION);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Given username with special characters, When generating and extracting, Then should preserve username")
        void givenUsernameWithSpecialCharacters_whenGeneratingAndExtracting_thenShouldPreserveUsername() {
            // Given
            String specialUsername = "user.name+test@example.com";
            UserDetails userDetails = User.builder()
                .username(specialUsername)
                .password("password")
                .authorities("USER")
                .build();

            // When
            String token = jwtOperations.generateToken(userDetails);
            String extractedUsername = jwtOperations.extractUsername(token);

            // Then
            assertThat(extractedUsername).isEqualTo(specialUsername);
        }

        @Test
        @DisplayName("Given empty extra claims map, When generating token, Then should generate valid token")
        void givenEmptyExtraClaimsMap_whenGeneratingToken_thenShouldGenerateValidToken() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            Map<String, Object> emptyClaims = new HashMap<>();

            // When
            String token = jwtOperations.generateToken(emptyClaims, userDetails);

            // Then
            assertThat(token).isNotNull();
            assertThat(jwtOperations.extractUsername(token)).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("Given null value in extra claims, When generating token, Then should generate token with null claim")
        void givenNullValueInExtraClaims_whenGeneratingToken_thenShouldGenerateTokenWithNullClaim() {
            // Given
            UserDetails userDetails = createTestUserDetails();
            Map<String, Object> claims = new HashMap<>();
            claims.put("nullClaim", null);

            // When
            String token = jwtOperations.generateToken(claims, userDetails);

            // Then
            Object nullValue = jwtOperations.extractClaim(token, c -> c.get("nullClaim"));
            assertThat(nullValue).isNull();
        }
    }
}
