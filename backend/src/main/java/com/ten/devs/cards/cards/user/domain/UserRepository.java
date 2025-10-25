package com.ten.devs.cards.cards.user.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
  void save(User user);

  List<User> findAll();

  List<User> findAllBySpecification(UserSpecification specification);

  Optional<User> getUserByUsername(String username);

  User getUserById(UserId id);
}
