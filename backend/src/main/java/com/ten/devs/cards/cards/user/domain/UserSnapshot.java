package com.ten.devs.cards.cards.user.domain;

import java.util.List;

public record UserSnapshot(UserId id, String username, String password, String firstName, String lastName, String email, List<Role> roles) {
}
