package com.ten.devs.cards.cards.user.presentation.request;

public record RegisterUserRequest(String username, String password, String email, String firstName, String lastName) {
}