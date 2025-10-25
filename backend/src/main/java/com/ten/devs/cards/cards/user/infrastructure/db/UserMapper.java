package com.ten.devs.cards.cards.user.infrastructure.db;


import com.ten.devs.cards.cards.user.domain.UserId;
import com.ten.devs.cards.cards.user.domain.UserSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
interface UserMapper {

    @Mapping(target = "authorities", ignore = true)
    UserEntity mapToEntity(UserSnapshot userSnapshot);

    UserSnapshot mapToSnapshot(UserEntity userEntity);

    default UserId mapToUserId(UUID id) {
        return UserId.of(id);
    }

    default UUID mapToUUID(UserId userId) {
        return userId.id();
    }
}

