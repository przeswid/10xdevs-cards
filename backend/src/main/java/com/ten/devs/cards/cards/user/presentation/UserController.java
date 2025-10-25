package com.ten.devs.cards.cards.user.presentation;

import an.awesome.pipelinr.Pipeline;
import com.ten.devs.cards.cards.user.application.query.GetUsersQuery;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;
import com.ten.devs.cards.cards.user.presentation.response.GetUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
class UserController {
    
    private final Pipeline cqsService;

    @GetMapping("/")
    public ResponseEntity<List<GetUserResponse>> allUsers() {
        GetUsersQuery query = new GetUsersQuery(null);
        List<UserSnapshot> userEntities = cqsService.send(query);

        return ResponseEntity.ok(userEntities.stream().map(userEntity ->
                new GetUserResponse(
                        userEntity.id(),
                        userEntity.username(),
                        userEntity.email(),
                        userEntity.firstName(),
                        userEntity.lastName()
                )).toList());
    }
}