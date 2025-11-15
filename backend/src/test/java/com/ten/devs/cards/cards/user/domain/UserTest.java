package com.ten.devs.cards.cards.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Domain Entity")
class UserTest {

    private static final UUID TEST_USER_UUID = UUID.randomUUID();
    private static final UserId TEST_USER_ID = new UserId(TEST_USER_UUID);
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "hashedPassword123";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String TEST_EMAIL = "john.doe@example.com";
    private static final List<Role> TEST_ROLES = List.of(Role.USER);

    @Nested
    @DisplayName("newUser() factory method")
    class NewUserMethod {

        @Test
        @DisplayName("Given valid user data, When creating new user, Then should create user with all fields")
        void givenValidUserData_whenCreatingNewUser_thenShouldCreateUserWithAllFields() {
            // When
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                TEST_ROLES
            );

            // Then
            assertThat(user).isNotNull();
            UserSnapshot snapshot = user.toSnapshot();
            assertThat(snapshot.id()).isEqualTo(TEST_USER_ID);
            assertThat(snapshot.username()).isEqualTo(TEST_USERNAME);
            assertThat(snapshot.password()).isEqualTo(TEST_PASSWORD);
            assertThat(snapshot.firstName()).isEqualTo(TEST_FIRST_NAME);
            assertThat(snapshot.lastName()).isEqualTo(TEST_LAST_NAME);
            assertThat(snapshot.email()).isEqualTo(TEST_EMAIL);
            assertThat(snapshot.roles()).containsExactly(Role.USER);
        }

        @Test
        @DisplayName("Given multiple roles, When creating new user, Then should create user with all roles")
        void givenMultipleRoles_whenCreatingNewUser_thenShouldCreateUserWithAllRoles() {
            // Given
            List<Role> multipleRoles = List.of(Role.USER, Role.ADMIN);

            // When
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                multipleRoles
            );

            // Then
            UserSnapshot snapshot = user.toSnapshot();
            assertThat(snapshot.roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
        }

        @Test
        @DisplayName("Given empty roles list, When creating new user, Then should create user with empty roles")
        void givenEmptyRolesList_whenCreatingNewUser_thenShouldCreateUserWithEmptyRoles() {
            // Given
            List<Role> emptyRoles = List.of();

            // When
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                emptyRoles
            );

            // Then
            UserSnapshot snapshot = user.toSnapshot();
            assertThat(snapshot.roles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("fromSnapshot() factory method")
    class FromSnapshotMethod {

        @Test
        @DisplayName("Given valid snapshot, When reconstructing user, Then should restore all state")
        void givenValidSnapshot_whenReconstructingUser_thenShouldRestoreAllState() {
            // Given
            UserSnapshot snapshot = new UserSnapshot(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                TEST_ROLES
            );

            // When
            User user = User.fromSnapshot(snapshot);

            // Then
            UserSnapshot reconstructedSnapshot = user.toSnapshot();
            assertThat(reconstructedSnapshot.id()).isEqualTo(TEST_USER_ID);
            assertThat(reconstructedSnapshot.username()).isEqualTo(TEST_USERNAME);
            assertThat(reconstructedSnapshot.password()).isEqualTo(TEST_PASSWORD);
            assertThat(reconstructedSnapshot.firstName()).isEqualTo(TEST_FIRST_NAME);
            assertThat(reconstructedSnapshot.lastName()).isEqualTo(TEST_LAST_NAME);
            assertThat(reconstructedSnapshot.email()).isEqualTo(TEST_EMAIL);
            assertThat(reconstructedSnapshot.roles()).containsExactly(Role.USER);
        }

        @Test
        @DisplayName("Given snapshot with roles list, When reconstructing user, Then should create defensive copy of roles")
        void givenSnapshotWithRolesList_whenReconstructingUser_thenShouldCreateDefensiveCopy() {
            // Given
            List<Role> mutableRoles = new ArrayList<>(List.of(Role.USER));
            UserSnapshot snapshot = new UserSnapshot(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                mutableRoles
            );

            // When
            User user = User.fromSnapshot(snapshot);
            mutableRoles.add(Role.ADMIN); // Modify original list

            // Then
            UserSnapshot reconstructedSnapshot = user.toSnapshot();
            assertThat(reconstructedSnapshot.roles()).containsExactly(Role.USER); // User copy unaffected
        }
    }

    @Nested
    @DisplayName("toSnapshot() snapshot pattern")
    class ToSnapshotMethod {

        @Test
        @DisplayName("Given user, When converting to snapshot, Then should capture all state")
        void givenUser_whenConvertingToSnapshot_thenShouldCaptureAllState() {
            // Given
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                TEST_ROLES
            );

            // When
            UserSnapshot snapshot = user.toSnapshot();

            // Then
            assertThat(snapshot.id()).isEqualTo(TEST_USER_ID);
            assertThat(snapshot.username()).isEqualTo(TEST_USERNAME);
            assertThat(snapshot.password()).isEqualTo(TEST_PASSWORD);
            assertThat(snapshot.firstName()).isEqualTo(TEST_FIRST_NAME);
            assertThat(snapshot.lastName()).isEqualTo(TEST_LAST_NAME);
            assertThat(snapshot.email()).isEqualTo(TEST_EMAIL);
            assertThat(snapshot.roles()).isNotNull();
        }

        @Test
        @DisplayName("Given user, When converting to snapshot, Then snapshot roles should be defensive copy")
        void givenUser_whenConvertingToSnapshot_thenSnapshotRolesShouldBeDefensiveCopy() {
            // Given
            List<Role> roles = new ArrayList<>(List.of(Role.USER));
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                roles
            );

            // When
            UserSnapshot snapshot = user.toSnapshot();
            roles.add(Role.ADMIN); // Modify original list

            // Then
            assertThat(snapshot.roles()).containsExactly(Role.USER); // Snapshot unaffected
        }

        @Test
        @DisplayName("Given user, When converting to snapshot multiple times, Then should create independent copies")
        void givenUser_whenConvertingToSnapshotMultipleTimes_thenShouldCreateIndependentCopies() {
            // Given
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                new ArrayList<>(List.of(Role.USER))
            );

            // When
            UserSnapshot snapshot1 = user.toSnapshot();
            UserSnapshot snapshot2 = user.toSnapshot();

            // Then
            assertThat(snapshot1).isNotSameAs(snapshot2); // Different instances
            assertThat(snapshot1.roles()).isNotSameAs(snapshot2.roles()); // Different role lists
            assertThat(snapshot1.roles()).isEqualTo(snapshot2.roles()); // But same content
        }
    }

    @Nested
    @DisplayName("Snapshot independence")
    class SnapshotIndependence {

        @Test
        @DisplayName("Given snapshot with roles, When snapshot roles list is accessed, Then should be independent of user internal state")
        void givenSnapshotWithRoles_whenSnapshotRolesListAccessed_thenShouldBeIndependentOfUserInternalState() {
            // Given
            User user = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                new ArrayList<>(List.of(Role.USER))
            );

            // When
            UserSnapshot snapshot1 = user.toSnapshot();
            UserSnapshot snapshot2 = user.toSnapshot();

            // Then
            assertThat(snapshot1.roles()).isNotSameAs(snapshot2.roles());
            assertThat(snapshot1.roles()).isEqualTo(snapshot2.roles());
        }
    }

    @Nested
    @DisplayName("Domain model behavior")
    class DomainModelBehavior {

        @Test
        @DisplayName("Given user with all fields, When creating snapshot and reconstructing, Then should preserve all data")
        void givenUserWithAllFields_whenCreatingSnapshotAndReconstructing_thenShouldPreserveAllData() {
            // Given
            User originalUser = User.newUser(
                TEST_USER_ID,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_EMAIL,
                List.of(Role.USER, Role.ADMIN)
            );

            // When
            UserSnapshot snapshot = originalUser.toSnapshot();
            User reconstructedUser = User.fromSnapshot(snapshot);

            // Then
            UserSnapshot reconstructedSnapshot = reconstructedUser.toSnapshot();
            assertThat(reconstructedSnapshot.id()).isEqualTo(snapshot.id());
            assertThat(reconstructedSnapshot.username()).isEqualTo(snapshot.username());
            assertThat(reconstructedSnapshot.password()).isEqualTo(snapshot.password());
            assertThat(reconstructedSnapshot.firstName()).isEqualTo(snapshot.firstName());
            assertThat(reconstructedSnapshot.lastName()).isEqualTo(snapshot.lastName());
            assertThat(reconstructedSnapshot.email()).isEqualTo(snapshot.email());
            assertThat(reconstructedSnapshot.roles()).containsExactlyInAnyOrderElementsOf(snapshot.roles());
        }
    }
}
