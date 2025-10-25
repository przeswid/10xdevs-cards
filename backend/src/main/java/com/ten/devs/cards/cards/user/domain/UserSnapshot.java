package com.ten.devs.cards.cards.user.domain;

import java.util.List;

public record UserSnapshot(UserId id, String username, String password, String email, String firstName, String lastName, List<Role> roles) {
}
