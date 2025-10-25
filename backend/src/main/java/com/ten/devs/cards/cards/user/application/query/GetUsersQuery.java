package com.ten.devs.cards.cards.user.application.query;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;

import java.util.List;

public record GetUsersQuery(String username) implements Command<List<UserSnapshot>> {
}
