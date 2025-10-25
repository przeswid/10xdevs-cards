package com.ten.devs.cards.cards.user.presentation.response;

import com.ten.devs.cards.cards.user.domain.UserId;

public record GetUserResponse(UserId id, String username, String email, String firstName, String lastName) {
}
