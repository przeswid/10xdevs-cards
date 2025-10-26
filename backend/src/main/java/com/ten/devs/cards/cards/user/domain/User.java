package com.ten.devs.cards.cards.user.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    private final UserId id;
    private final String username;

    private String password;
    private String firstName;
    private String lastName;

    private String email;

    private final List<Role> roles;

    public static User newUser(UserId userId, String username, String password, String firstName, String lastName, String email, List<Role> roles) {
        return new User(userId, username, password, firstName, lastName, email, roles);
    }

    public static User fromSnapshot(UserSnapshot userSnapshot) {
        return new User(userSnapshot.id(), userSnapshot.username(), userSnapshot.password(), userSnapshot.firstName(), userSnapshot.lastName(), userSnapshot.email(), new ArrayList<>(userSnapshot.roles()));
    }

    public UserSnapshot toSnapshot() {
        return new UserSnapshot(
                this.id,
                this.username,
                this.password,
                this.firstName,
                this.lastName,
                this.email,
                new ArrayList<>(this.roles)
        );
    }


}
